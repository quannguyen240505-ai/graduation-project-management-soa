import { Component, inject, OnInit, signal } from "@angular/core";

import { CommonModule } from "@angular/common";

import { HttpClient } from "@angular/common/http";

import { finalize } from "rxjs";

interface HealthResponse {
  status: string;
}

interface UserMeResponse {
  id: number;
  userCode: string;
  fullName: string;
  email: string;
  role: string;
  status: string;
}

@Component({
  selector: "app-gateway-check",
  standalone: true,

  imports: [CommonModule],

  templateUrl: "./gateway-check.component.html",

  styleUrl: "./gateway-check.component.css",
})
export class GatewayCheckComponent implements OnInit {
  private readonly http = inject(HttpClient);

  readonly loading = signal(false);

  readonly gatewayOnline = signal(false);

  readonly authOnline = signal(false);

  readonly gatewayStatus = signal("Chưa kiểm tra");

  readonly authStatus = signal("Chưa kiểm tra");

  readonly checkedAt = signal<Date | null>(null);

  ngOnInit(): void {
    this.checkSystem();
  }

  checkSystem(): void {
    this.loading.set(true);

    this.gatewayOnline.set(false);
    this.authOnline.set(false);

    this.gatewayStatus.set("Đang kiểm tra...");

    this.authStatus.set("Đang kiểm tra...");

    this.checkGateway();
    this.checkAuth();
  }

  private checkGateway(): void {
    this.http.get<HealthResponse>("/actuator/health").subscribe({
      next: (response) => {
        const online = response.status === "UP";

        this.gatewayOnline.set(online);

        this.gatewayStatus.set(
          online ? "Hoạt động bình thường" : "Không khả dụng",
        );
      },

      error: () => {
        this.gatewayOnline.set(false);

        this.gatewayStatus.set("Không thể kết nối");
      },
    });
  }

  private checkAuth(): void {
    this.http
      .get<UserMeResponse>("/api/users/me")
      .pipe(
        finalize(() => {
          this.loading.set(false);

          this.checkedAt.set(new Date());
        }),
      )
      .subscribe({
        next: () => {
          this.authOnline.set(true);

          this.authStatus.set("Hoạt động bình thường");
        },

        error: () => {
          this.authOnline.set(false);

          this.authStatus.set("Không thể kết nối");
        },
      });
  }
}
