import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';

import { NotificationService } from '../../core/services/notification.service';
import { Notification } from '../../core/models/notification.model';

@Component({
    selector: 'app-notifications',
    standalone: true,
    imports: [CommonModule],
    templateUrl: './notifications.component.html',
    styleUrl: './notifications.component.css'
})
export class NotificationsComponent implements OnInit {

    private readonly notificationService =
        inject(NotificationService);

    notifications: Notification[] = [];
    loading = false;
    error = '';

    ngOnInit(): void {
        this.loadNotifications();
    }

    loadNotifications(): void {
        this.loading = true;
        this.error = '';

        this.notificationService.getAll().subscribe({
            next: data => {
                this.notifications = data;
                this.loading = false;
            },
            error: () => {
                this.error = 'Không thể tải danh sách thông báo.';
                this.loading = false;
            }
        });
    }

    markAsRead(notification: Notification): void {
        if (notification.read) {
            return;
        }

        this.notificationService
            .markAsRead(notification.id)
            .subscribe({
                next: updated => {
                    notification.read = updated.read;
                },
                error: () => {
                    this.error = 'Không thể đánh dấu thông báo đã đọc.';
                }
            });
    }

    get unreadCount(): number {
        return this.notifications
            .filter(notification => !notification.read)
            .length;
    }
}