package com.group.registration.producer;

import com.group.registration.event.RegistrationCreatedEvent;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

/**
 * Publishes registration events to RabbitMQ.
 */
@Component
public class RegistrationEventProducer {

    private static final String EXCHANGE =
            "registration.exchange";

    private static final String ROUTING_KEY =
            "registration.created";

    private final RabbitTemplate rabbitTemplate;

    public RegistrationEventProducer(
            RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    /**
     * Publishes an event after a registration is created successfully.
     *
     * @param event registration-created event
     */
    public void publishRegistrationCreated(
            RegistrationCreatedEvent event) {

        rabbitTemplate.convertAndSend(
                EXCHANGE,
                ROUTING_KEY,
                event
        );
    }
}