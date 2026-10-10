import { Component, computed, inject } from "@angular/core";

import { RouterLink } from "@angular/router";

import { AuthService } from "../../core/services/auth.service";

@Component({
  selector: "app-dashboard",

  standalone: true,

  imports: [RouterLink],

  templateUrl: "./dashboard.component.html",

  styleUrl: "./dashboard.component.css",
})
export class DashboardComponent {
  readonly auth = inject(AuthService);

  readonly session = computed(() => this.auth.session());

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
}
