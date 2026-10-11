package com.graduation.notification.consumer;

import com.graduation.notification.config.RabbitMqConfig;
import com.graduation.notification.event.RegistrationCreatedEvent;
import com.graduation.notification.event.RegistrationStatusChangedEvent;
import com.graduation.notification.service.NotificationService;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class RegistrationEventConsumer {

    private final NotificationService notificationService;

    public RegistrationEventConsumer(
            NotificationService notificationService) {

        this.notificationService =
                notificationService;
    }

    // =====================================
    // Sinh viên đăng ký đề tài
    // =====================================

    @RabbitListener(
            queues =
                    RabbitMqConfig.NOTIFICATION_REGISTRATION_QUEUE
    )
    public void handleRegistrationCreated(
            RegistrationCreatedEvent event) {

        String topicLabel =
                event.getTopicName() != null
                        && !event.getTopicName().isBlank()
                        ? "\"" + event.getTopicName() + "\""
                        : "ID " + event.getTopicId();

        notificationService.create(
                event.getUserId(),
                "Đăng ký đề tài thành công",
                "Bạn đã đăng ký đề tài "
                        + topicLabel
                        + " thành công."
        );
    }

    // =====================================
    // Lecturer/Admin duyệt hoặc từ chối
    // =====================================

    @RabbitListener(
            queues =
                    RabbitMqConfig.NOTIFICATION_STATUS_QUEUE
    )
    public void handleRegistrationStatusChanged(
            RegistrationStatusChangedEvent event) {

        if ("APPROVED".equals(event.getStatus())) {

            notificationService.create(
                    event.getUserId(),
                    "Đăng ký đề tài được chấp nhận",
                    "Đăng ký đề tài ID "
                            + event.getTopicId()
                            + " của bạn đã được chấp nhận."
            );

            return;
        }

        if ("REJECTED".equals(event.getStatus())) {

            notificationService.create(
                    event.getUserId(),
                    "Đăng ký đề tài bị từ chối",
                    "Đăng ký đề tài ID "
                            + event.getTopicId()
                            + " của bạn đã bị từ chối."
            );
        }
    }
}