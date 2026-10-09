package com.graduation.notification.producer;

import com.graduation.notification.config.RabbitMqConfig;
import com.graduation.notification.event.RegistrationCreatedEvent;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

/**
 * Publishes registration-created events.
 *
 * <p>Use this producer for local/demo testing. In the final integrated
 * architecture, equivalent publishing logic should live in registration-service.</p>
 */
@Component
public class RegistrationEventProducer {

    private final RabbitTemplate rabbitTemplate;

    public RegistrationEventProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publish(RegistrationCreatedEvent event) {
        rabbitTemplate.convertAndSend(
                RabbitMqConfig.REGISTRATION_EXCHANGE,
                RabbitMqConfig.REGISTRATION_CREATED_ROUTING_KEY,
                event
        );
    }
}
