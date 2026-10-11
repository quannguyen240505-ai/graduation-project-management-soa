package com.group.registration.producer;

import com.group.registration.config.RabbitMqConfig;
import com.group.registration.event.RegistrationCreatedEvent;
import com.group.registration.event.RegistrationStatusChangedEvent;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class RegistrationEventProducer {

    private final RabbitTemplate rabbitTemplate;

    public RegistrationEventProducer(
            RabbitTemplate rabbitTemplate) {

        this.rabbitTemplate = rabbitTemplate;
    }

    public void publish(
            RegistrationCreatedEvent event) {

        rabbitTemplate.convertAndSend(
                RabbitMqConfig.REGISTRATION_EXCHANGE,
                RabbitMqConfig.REGISTRATION_CREATED_ROUTING_KEY,
                event
        );
    }

    public void publishStatusChanged(
            RegistrationStatusChangedEvent event) {

        rabbitTemplate.convertAndSend(
                RabbitMqConfig.REGISTRATION_EXCHANGE,
                RabbitMqConfig.REGISTRATION_STATUS_CHANGED_ROUTING_KEY,
                event
        );
    }
}