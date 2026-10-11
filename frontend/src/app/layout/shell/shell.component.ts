import { Component, computed, inject } from "@angular/core";
import { toSignal } from "@angular/core/rxjs-interop";

import {
  NavigationEnd,
  Router,
  RouterLink,
  RouterLinkActive,
  RouterOutlet,
} from "@angular/router";

import { filter, map } from "rxjs";
import { AuthService } from "../../core/services/auth.service";

@Component({
  selector: "app-shell",
  standalone: true,
  imports: [RouterOutlet, RouterLink, RouterLinkActive],
  templateUrl: "./shell.component.html",
  styleUrl: "./shell.component.css",
})
export class ShellComponent {
  readonly auth = inject(AuthService);
  private readonly router = inject(Router);

  readonly session = computed(() => this.auth.session());

  private readonly currentUrl = toSignal(
    this.router.events.pipe(
      filter((event): event is NavigationEnd => event instanceof NavigationEnd),
      map((event) => event.urlAfterRedirects),
    ),
    { initialValue: this.router.url },
  );

  readonly pageTitle = computed(() => {
    const path = this.currentUrl().split(/[?#]/)[0];

    const titles: Record<string, string> = {
      "/dashboard": "Bảng điều khiển",
      "/topics": "Đề tài tốt nghiệp",
      "/registrations": "Đăng ký đề tài",
      "/notifications": "Thông báo",
      "/profile": "Tài khoản cá nhân",
      "/gateway-check": "Trạng thái hệ thống",
    };

    return titles[path] ?? "Hệ thống quản lý đồ án";
  });

  logout(): void {
    this.auth.logout();
    void this.router.navigate(["/login"]);
  }
}
