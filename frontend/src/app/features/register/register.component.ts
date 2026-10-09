import { Component, inject, signal } from "@angular/core";

import { CommonModule } from "@angular/common";

import { FormBuilder, ReactiveFormsModule, Validators } from "@angular/forms";

import { Router, RouterLink } from "@angular/router";

import { finalize } from "rxjs";

import { AuthService } from "../../core/services/auth.service";

@Component({
  selector: "app-register",
  standalone: true,

  imports: [CommonModule, ReactiveFormsModule, RouterLink],

  templateUrl: "./register.component.html",
  styleUrl: "./register.component.css",
})
export class RegisterComponent {
  private readonly fb = inject(FormBuilder);

  private readonly auth = inject(AuthService);

  private readonly router = inject(Router);

  readonly loading = signal(false);

  readonly error = signal("");

  readonly form = this.fb.nonNullable.group({
    userCode: ["", [Validators.required, Validators.maxLength(30)]],

    fullName: ["", [Validators.required, Validators.maxLength(100)]],

    email: [
      "",
      [Validators.required, Validators.email, Validators.maxLength(100)],
    ],

    phone: ["", [Validators.maxLength(20)]],

    className: ["", [Validators.maxLength(50)]],

    department: ["", [Validators.maxLength(100)]],

    password: [
      "",
      [Validators.required, Validators.minLength(6), Validators.maxLength(100)],
    ],

    confirmPassword: ["", [Validators.required]],
  });

  submit(): void {
    if (this.form.invalid || this.loading()) {
      this.form.markAllAsTouched();

      return;
    }

    const value = this.form.getRawValue();

    if (value.password !== value.confirmPassword) {
      this.error.set("Mật khẩu xác nhận không khớp.");

      return;
    }

    this.loading.set(true);

    this.error.set("");

    this.auth
      .register({
        userCode: value.userCode.trim(),

        password: value.password,

        fullName: value.fullName.trim(),

        email: value.email.trim(),

        phone: value.phone.trim() || undefined,

        className: value.className.trim() || undefined,

        department: value.department.trim() || undefined,
      })
      .pipe(finalize(() => this.loading.set(false)))
      .subscribe({
        next: () => {
          void this.router.navigate(["/login"], {
            queryParams: {
              registered: "true",
            },
          });
        },

        error: (err) => {
          const message =
            err?.error?.message ||
            err?.error?.detail ||
            err?.error?.error ||
            "Đăng ký thất bại. Vui lòng kiểm tra lại thông tin.";

          this.error.set(String(message));
        },
      });
  }
}
