package com.graduation.notification.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfig {

    public static final String REGISTRATION_EXCHANGE =
            "registration.exchange";

    // ===== Registration Created =====

    public static final String NOTIFICATION_REGISTRATION_QUEUE =
            "notification.registration.queue";

    public static final String REGISTRATION_CREATED_ROUTING_KEY =
            "registration.created";

    // ===== Registration Status Changed =====

    public static final String NOTIFICATION_STATUS_QUEUE =
            "notification.registration.status.queue";

    public static final String REGISTRATION_STATUS_CHANGED_ROUTING_KEY =
            "registration.status.changed";

    @Bean
    public DirectExchange registrationExchange() {
        return new DirectExchange(
                REGISTRATION_EXCHANGE,
                true,
                false
        );
    }

    @Bean
    public Queue notificationRegistrationQueue() {
        return QueueBuilder
                .durable(NOTIFICATION_REGISTRATION_QUEUE)
                .build();
    }

    @Bean
    public Binding registrationCreatedBinding(
            Queue notificationRegistrationQueue,
            DirectExchange registrationExchange) {

        return BindingBuilder
                .bind(notificationRegistrationQueue)
                .to(registrationExchange)
                .with(REGISTRATION_CREATED_ROUTING_KEY);
    }

    @Bean
    public Queue notificationStatusQueue() {
        return QueueBuilder
                .durable(NOTIFICATION_STATUS_QUEUE)
                .build();
    }

    @Bean
    public Binding registrationStatusBinding(
            Queue notificationStatusQueue,
            DirectExchange registrationExchange) {

        return BindingBuilder
                .bind(notificationStatusQueue)
                .to(registrationExchange)
                .with(REGISTRATION_STATUS_CHANGED_ROUTING_KEY);
    }

    @Bean
    public MessageConverter rabbitMessageConverter() {
        return new JacksonJsonMessageConverter();
    }
}