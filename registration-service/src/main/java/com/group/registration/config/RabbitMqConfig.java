package com.group.registration.config;

import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ configuration for publishing registration events.
 */
@Configuration
public class RabbitMqConfig {

    /**
     * Configures RabbitMQ messages to use JSON.
     *
     * @return JSON message converter
     */
    @Bean
    public MessageConverter rabbitMessageConverter() {
        return new JacksonJsonMessageConverter();
    }
}