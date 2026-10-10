import { DatePipe } from "@angular/common";
import { Component, DestroyRef, OnInit, computed, inject, signal } from "@angular/core";
import { takeUntilDestroyed } from "@angular/core/rxjs-interop";
import { ActivatedRoute, Router, RouterLink } from "@angular/router";
import { BehaviorSubject, catchError, combineLatest, finalize, of, switchMap } from "rxjs";
import { Topic } from "../../core/models/topic.models";
import { AuthService } from "../../core/services/auth.service";
import { TopicService } from "../../core/services/topic.service";
import { TopicDeleteDialogComponent } from "./topic-delete-dialog.component";
import { canManageTopics, hasAvailableSlots, parseTopicId, topicErrorMessage,
  topicOccupancy, topicStatusLabel } from "./topic.utils";

@Component({
  selector: "app-topic-detail",
  standalone: true,
  imports: [DatePipe, RouterLink, TopicDeleteDialogComponent],
  templateUrl: "./topic-detail.component.html",
  styleUrls: ["./topic-page.css", "./topic-detail.component.css"],
})
export class TopicDetailComponent implements OnInit {
  private readonly api = inject(TopicService);
  private readonly auth = inject(AuthService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly destroyRef = inject(DestroyRef);
  private readonly refresh = new BehaviorSubject(0);
  readonly topic = signal<Topic | null>(null);
  readonly loading = signal(true);
  readonly error = signal("");
  readonly deleting = signal(false);
  readonly canManage = computed(() => canManageTopics(this.auth.session()?.role));
  readonly notice = this.route.snapshot.queryParamMap.get("notice") === "created"
    ? "Đã tạo đề tài thành công." : this.route.snapshot.queryParamMap.get("notice") === "updated"
      ? "Đã cập nhật đề tài thành công." : "";
  readonly statusLabel = topicStatusLabel;
  readonly hasSlots = hasAvailableSlots;
  readonly occupancy = topicOccupancy;

  ngOnInit(): void {
    combineLatest([this.route.paramMap, this.refresh]).pipe(
      switchMap(([params]) => {
        this.topic.set(null);
        this.deleting.set(false);
        this.error.set("");
        const id = parseTopicId(params.get("id"));
        if (id === null) {
          this.loading.set(false);
          this.error.set("Mã số đề tài không hợp lệ.");
          return of(null);
        }
        this.loading.set(true);
        return this.api.getById(id).pipe(
          catchError((error: unknown) => {
            this.error.set(topicErrorMessage(error, "load"));
            return of(null);
          }),
          finalize(() => this.loading.set(false)),
        );
      }),
      takeUntilDestroyed(this.destroyRef),
    ).subscribe(topic => this.topic.set(topic));
  }

  reload(): void { this.refresh.next(this.refresh.value + 1); }

  deleted(): void {
    this.deleting.set(false);
    void this.router.navigate(["/topics"], { queryParams: { notice: "deleted" } });
  }
}
