package com.graduation.notification.controller;

import com.graduation.notification.entity.Notification;
import com.graduation.notification.service.NotificationService;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(
            NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    // ADMIN dùng khi cần xem toàn bộ
    @GetMapping
    public List<Notification> getAllNotifications() {
        return notificationService.getAllNotifications();
    }

    // Người dùng xem thông báo của chính mình
    @GetMapping("/me")
    public List<Notification> getMyNotifications(
            @AuthenticationPrincipal Jwt jwt) {

        Long userId = getUserId(jwt);

        return notificationService
                .getNotificationsByUser(userId);
    }

    @PutMapping("/{id}/read")
    public Notification markAsRead(
            @PathVariable Long id,
            @AuthenticationPrincipal Jwt jwt) {

        Long userId = getUserId(jwt);

        return notificationService
                .markAsRead(id, userId);
    }

    private Long getUserId(Jwt jwt) {
        Object claim = jwt.getClaim("userId");

        if (!(claim instanceof Number number)) {
            throw new IllegalArgumentException(
                    "JWT không chứa userId hợp lệ");
        }

        return number.longValue();
    }
}