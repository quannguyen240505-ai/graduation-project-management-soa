import { HttpErrorResponse } from "@angular/common/http";
import { Component, DestroyRef, OnInit, inject, signal } from "@angular/core";
import { takeUntilDestroyed } from "@angular/core/rxjs-interop";
import { AbstractControl, FormBuilder, ReactiveFormsModule, ValidationErrors, Validators } from "@angular/forms";
import { ActivatedRoute, Router, RouterLink } from "@angular/router";
import { BehaviorSubject, catchError, combineLatest, finalize, of, switchMap, throwError } from "rxjs";
import { Topic, TopicRequest } from "../../core/models/topic.models";
import { AuthService } from "../../core/services/auth.service";
import { TopicService } from "../../core/services/topic.service";
import { parseTopicId, topicErrorMessage } from "./topic.utils";

/** Validators.required không loại chuỗi chỉ gồm dấu cách. */
function notBlank(control: AbstractControl): ValidationErrors | null {
  return typeof control.value === "string" && !control.value.trim() ? { blank: true } : null;
}

function wholeNumber(control: AbstractControl): ValidationErrors | null {
  return control.value == null || Number.isSafeInteger(control.value) ? null : { wholeNumber: true };
}

class LecturerValidationError extends Error {}

@Component({
  selector: "app-topic-form",
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: "./topic-form.component.html",
  styleUrls: ["./topic-page.css", "./topic-form.component.css"],
})
export class TopicFormComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly api = inject(TopicService);
  private readonly auth = inject(AuthService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly destroyRef = inject(DestroyRef);
  private readonly refresh = new BehaviorSubject(0);
  readonly existing = signal<Topic | null>(null);
  readonly editing = signal(false);
  readonly loading = signal(true);
  readonly saving = signal(false);
  readonly loadError = signal("");
  readonly error = signal("");
  readonly form = this.fb.group({
    topicCode: this.fb.nonNullable.control("", [Validators.required, notBlank, Validators.maxLength(50)]),
    topicName: this.fb.nonNullable.control("", [Validators.required, notBlank, Validators.maxLength(255)]),
    description: this.fb.nonNullable.control(""),
    lecturerId: this.fb.control<number | null>(null, [Validators.required, Validators.min(1), wholeNumber]),
    maxStudents: this.fb.control<number | null>(5, [Validators.required, Validators.min(1), Validators.max(2147483647), wholeNumber]),
    field: this.fb.nonNullable.control("", [Validators.maxLength(100)]),
  });

  ngOnInit(): void {
    combineLatest([this.route.paramMap, this.refresh]).pipe(
      switchMap(([params]) => {
        this.loadError.set(""); this.error.set(""); this.existing.set(null);
        this.editing.set(params.has("id"));
        if (!this.editing()) {
          this.form.reset({ topicCode: "", topicName: "", description: "", field: "", maxStudents: 5,
            lecturerId: this.auth.session()?.role === "LECTURER" ? this.auth.session()!.userId : null });
          this.loading.set(false);
          return of(null);
        }
        const id = parseTopicId(params.get("id"));
        if (id === null) {
          this.loading.set(false); this.loadError.set("Mã số đề tài không hợp lệ.");
          return of(null);
        }
        this.loading.set(true);
        return this.api.getById(id).pipe(
          catchError((error: unknown) => {
            this.loadError.set(topicErrorMessage(error, "load")); return of(null);
          }),
          finalize(() => this.loading.set(false)),
        );
      }),
      takeUntilDestroyed(this.destroyRef),
    ).subscribe(topic => {
      if (!topic) return;
      this.existing.set(topic);
      this.form.controls.maxStudents.setValidators([Validators.required,
        Validators.min(Math.max(1, topic.currentStudents)), Validators.max(2147483647), wholeNumber]);
      this.form.reset({ topicCode: topic.topicCode, topicName: topic.topicName,
        description: topic.description ?? "", lecturerId: topic.lecturerId,
        maxStudents: topic.maxStudents, field: topic.field ?? "" });
    });
  }

  invalid(key: keyof typeof this.form.controls): boolean {
    const control = this.form.controls[key];
    return control.touched && control.invalid;
  }

  reload(): void { this.refresh.next(this.refresh.value + 1); }

  cancel(): void {
    if (this.saving()) return;
    const topic = this.existing();
    void this.router.navigate(topic ? ["/topics", topic.id] : ["/topics"]);
  }

  submit(): void {
    if (this.saving() || this.loading() || this.loadError()) return;
    if (this.form.invalid) { this.form.markAllAsTouched(); return; }
    const value = this.form.getRawValue();
    if (value.lecturerId == null || value.maxStudents == null) return;
    const request: TopicRequest = {
      topicCode: value.topicCode.trim(), topicName: value.topicName.trim(),
      description: value.description.trim(), lecturerId: value.lecturerId,
      maxStudents: value.maxStudents, field: value.field.trim(),
    };
    const existing = this.existing();
    this.error.set(""); this.saving.set(true);
    // Kiểm tra để có lỗi dễ hiểu trên FE; backend vẫn kiểm tra lại khi ghi dữ liệu.
    this.api.getLecturer(request.lecturerId).pipe(
      catchError((error: unknown) => {
        return throwError(() => error instanceof HttpErrorResponse && error.status === 404
          ? new LecturerValidationError("Không tìm thấy tài khoản giảng viên. Hãy kiểm tra mã số tài khoản.")
          : error);
      }),
      switchMap(user => {
        if (user.id !== request.lecturerId || user.role !== "LECTURER") {
          return throwError(() => new LecturerValidationError("Tài khoản được chọn phải có vai trò Giảng viên."));
        }
        if (user.status !== "ACTIVE") {
          return throwError(() => new LecturerValidationError("Tài khoản giảng viên đang không hoạt động."));
        }
        return existing ? this.api.update(existing.id, request) : this.api.create(request);
      }),
      takeUntilDestroyed(this.destroyRef),
      finalize(() => this.saving.set(false)),
    ).subscribe({
      next: topic => void this.router.navigate(["/topics", topic.id], {
        queryParams: { notice: existing ? "updated" : "created" },
      }),
      error: (error: unknown) => this.error.set(error instanceof LecturerValidationError
        ? error.message : topicErrorMessage(error, "save")),
    });
  }
}
