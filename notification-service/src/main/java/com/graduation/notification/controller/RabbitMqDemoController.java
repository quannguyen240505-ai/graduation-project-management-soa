package com.graduation.notification.controller;

import com.graduation.notification.event.RegistrationCreatedEvent;
import com.graduation.notification.producer.RegistrationEventProducer;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Collections;

import java.util.Map;

/**
 * Temporary Postman endpoint for demonstrating RabbitMQ end-to-end.
 */
@RestController
@RequestMapping("/api/notifications/demo")
public class RabbitMqDemoController {

    private final RegistrationEventProducer producer;

    public RabbitMqDemoController(RegistrationEventProducer producer) {
        this.producer = producer;
    }

    @PostMapping("/registration-created")
    public ResponseEntity<Map<String, String>> publishRegistrationCreated(
            @RequestBody RegistrationCreatedEvent event) {

        producer.publish(event);
        return ResponseEntity.accepted().body(
                Collections.singletonMap(
                        "message",
                        "registration.created event published"
                )
        );
    }
}
