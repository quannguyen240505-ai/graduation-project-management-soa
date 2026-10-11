import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { Notification } from '../models/notification.model';

@Injectable({
    providedIn: 'root'
})
export class NotificationService {

    private readonly http = inject(HttpClient);

    getAll(): Observable<Notification[]> {
        return this.http.get<Notification[]>('/api/notifications');
    }

    getByUser(userId: number): Observable<Notification[]> {
        return this.http.get<Notification[]>(
            `/api/notifications/user/${userId}`
        );
    }

    markAsRead(id: number): Observable<Notification> {
        return this.http.put<Notification>(
            `/api/notifications/${id}/read`,
            {}
        );
    }
}