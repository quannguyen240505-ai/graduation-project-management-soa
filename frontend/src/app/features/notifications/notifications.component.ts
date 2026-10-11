import { CommonModule } from "@angular/common";
import { Component, OnInit, inject } from "@angular/core";

import { Notification } from "../../core/models/notification.model";
import { NotificationService } from "../../core/services/notification.service";

@Component({
  selector: "app-notifications",
  standalone: true,
  imports: [CommonModule],
  templateUrl: "./notifications.component.html",
  styleUrl: "./notifications.component.css",
})
export class NotificationsComponent implements OnInit {
  private readonly notificationService = inject(NotificationService);

  notifications: Notification[] = [];
  loading = false;
  error = "";

  ngOnInit(): void {
    this.loadNotifications();
  }

  get unreadCount(): number {
    return this.notifications.filter((notification) => !notification.read)
      .length;
  }

  loadNotifications(): void {
    this.loading = true;
    this.error = "";

    this.notificationService.getMine().subscribe({
      next: (data: Notification[]) => {
        this.notifications = data;
        this.loading = false;
      },

      error: () => {
        this.error = "Không thể tải danh sách thông báo.";
        this.loading = false;
      },
    });
  }

  markAsRead(id: number): void {
    this.notificationService.markAsRead(id).subscribe({
      next: (updated: Notification) => {
        this.notifications = this.notifications.map((notification) =>
          notification.id === updated.id ? updated : notification,
        );
      },

      error: () => {
        this.error = "Không thể đánh dấu thông báo đã đọc.";
      },
    });
  }
}
