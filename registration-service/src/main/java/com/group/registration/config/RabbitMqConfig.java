package com.group.registration.config;

import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfig {

    public static final String REGISTRATION_EXCHANGE =
            "registration.exchange";

    public static final String REGISTRATION_CREATED_ROUTING_KEY =
            "registration.created";

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
    public MessageConverter rabbitMessageConverter() {
        return new JacksonJsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(
            ConnectionFactory connectionFactory,
            MessageConverter rabbitMessageConverter) {

        RabbitTemplate template =
                new RabbitTemplate(connectionFactory);

        template.setMessageConverter(
                rabbitMessageConverter
        );

        return template;
    }
}