package com.graduation.notification.service;

import com.graduation.notification.entity.Notification;
import com.graduation.notification.repository.NotificationRepository;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Provides notification-related business logic.
 */
@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public NotificationService(
            NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    public List<Notification> getAllNotifications() {
        return notificationRepository.findAll();
    }

    public List<Notification> getNotificationsByUser(Long userId) {
        return notificationRepository
                .findByUserIdOrderByCreatedAtDesc(userId);
    }

    public Notification markAsRead(Long id) {
        Notification notification = notificationRepository
                .findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Notification not found"));

        notification.setRead(true);

        return notificationRepository.save(notification);
    }

    public Notification create(
            Long userId,
            String title,
            String content) {

        Notification notification =
                new Notification(userId, title, content);

        return notificationRepository.save(notification);
    }
}