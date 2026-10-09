import { Component, inject, OnInit, signal } from "@angular/core";

import { CommonModule } from "@angular/common";

import { AuthService } from "../../core/services/auth.service";

import { UserMeResponse } from "../../core/models/auth.models";

@Component({
  selector: "app-profile",
  standalone: true,

  imports: [CommonModule],

  templateUrl: "./profile.component.html",
  styleUrl: "./profile.component.css",
})
export class ProfileComponent implements OnInit {
  private readonly auth = inject(AuthService);

  readonly user = signal<UserMeResponse | null>(null);

  readonly loading = signal(false);

  readonly error = signal("");

  ngOnInit(): void {
    this.loadProfile();
  }

  loadProfile(): void {
    this.loading.set(true);
    this.error.set("");

    this.auth.getMe().subscribe({
      next: (user) => {
        this.user.set(user);
        this.loading.set(false);
      },

      error: (err) => {
        this.loading.set(false);

        this.error.set(
          err?.error?.message || "Không thể tải thông tin tài khoản.",
        );
      },
    });
  }

  roleLabel(role: string | undefined): string {
    switch (role) {
      case "STUDENT":
        return "Sinh viên";

      case "LECTURER":
        return "Giảng viên";

      case "ADMIN":
        return "Quản trị viên";

      default:
        return role || "Không xác định";
    }
  }

  getInitials(fullName: string | undefined): string {
    if (!fullName) {
      return "U";
    }

    const parts = fullName.trim().split(/\s+/);

    if (parts.length === 1) {
      return parts[0][0].toUpperCase();
    }

    return (parts[0][0] + parts[parts.length - 1][0]).toUpperCase();
  }
}
