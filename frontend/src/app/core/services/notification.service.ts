import { HttpClient } from "@angular/common/http";
import { Injectable, inject } from "@angular/core";
import { Observable } from "rxjs";

import { Notification } from "../models/notification.model";

@Injectable({
  providedIn: "root",
})
export class NotificationService {
  private readonly http = inject(HttpClient);

  getMine(): Observable<Notification[]> {
    return this.http.get<Notification[]>("/api/notifications/me");
  }

  markAsRead(id: number): Observable<Notification> {
    return this.http.put<Notification>(`/api/notifications/${id}/read`, {});
  }
}
