package com.graduation.notification.controller;

import com.graduation.notification.entity.Notification;
import com.graduation.notification.service.NotificationService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for notifications.
 */
@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(
            NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public List<Notification> getAllNotifications() {
        return notificationService.getAllNotifications();
    }

    @GetMapping("/user/{userId}")
    public List<Notification> getByUser(
            @PathVariable Long userId) {
        return notificationService
                .getNotificationsByUser(userId);
    }

    @PutMapping("/{id}/read")
    public Notification markAsRead(
            @PathVariable Long id) {
        return notificationService.markAsRead(id);
    }
}