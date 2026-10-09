import { Component, inject, signal } from "@angular/core";
import { CommonModule } from "@angular/common";
import { FormBuilder, ReactiveFormsModule, Validators } from "@angular/forms";
import { ActivatedRoute, Router, RouterLink } from "@angular/router";
import { finalize } from "rxjs";
import { AuthService } from "../../core/services/auth.service";

@Component({
  selector: "app-login",
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: "./login.component.html",
  styleUrl: "./login.component.css",
})
export class LoginComponent {
  private readonly fb = inject(FormBuilder);
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);

  readonly registered =
    this.route.snapshot.queryParamMap.get("registered") === "true";
  readonly loading = signal(false);
  readonly error = signal("");
  readonly expired =
    this.route.snapshot.queryParamMap.get("reason") === "expired";

  readonly form = this.fb.nonNullable.group({
    userCode: ["", [Validators.required]],
    password: ["", [Validators.required, Validators.minLength(4)]],
  });

  submit(): void {
    if (this.form.invalid || this.loading()) {
      this.form.markAllAsTouched();
      return;
    }

    this.loading.set(true);
    this.error.set("");

    this.auth
      .login(this.form.getRawValue())
      .pipe(finalize(() => this.loading.set(false)))
      .subscribe({
        next: () => void this.router.navigate(["/dashboard"]),
        error: (err) => {
          const message =
            err?.error?.message ||
            err?.error?.detail ||
            "Đăng nhập thất bại. Kiểm tra userCode, mật khẩu và trạng thái các service.";
          this.error.set(String(message));
        },
      });
  }
}
