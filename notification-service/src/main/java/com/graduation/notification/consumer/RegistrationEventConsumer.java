package com.graduation.notification.consumer;

import com.graduation.notification.config.RabbitMqConfig;
import com.graduation.notification.event.RegistrationCreatedEvent;
import com.graduation.notification.service.NotificationService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * Consumes registration-created events and creates notifications.
 */
@Component
public class RegistrationEventConsumer {

    private final NotificationService notificationService;

    public RegistrationEventConsumer(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @RabbitListener(queues = RabbitMqConfig.NOTIFICATION_REGISTRATION_QUEUE)
    public void handleRegistrationCreated(RegistrationCreatedEvent event) {
        String topicLabel =
                event.getTopicName() != null && !event.getTopicName().trim().isEmpty()
                        ? "\"" + event.getTopicName() + "\""
                        : "ID " + event.getTopicId();

        notificationService.create(
                event.getUserId(),
                "Dang ky de tai thanh cong",
                "Ban da dang ky de tai " + topicLabel + " thanh cong."
        );
    }
}
