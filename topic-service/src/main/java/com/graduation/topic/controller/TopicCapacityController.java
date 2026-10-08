package com.graduation.topic.controller;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.graduation.topic.dto.TopicCapacityRequest;
import com.graduation.topic.dto.TopicResponse;
import com.graduation.topic.service.TopicCapacityService;

/** API gọi giữa Registration và Topic qua Eureka, không mở qua Gateway. */
@RestController
@RequestMapping("/api/topics")
public class TopicCapacityController {

    private final TopicCapacityService service;

    public TopicCapacityController(TopicCapacityService service) {
        this.service = service;
    }

    @PutMapping("/{topicId}/registrations/{registrationId}/capacity")
    public TopicResponse synchronize(
            @PathVariable("topicId") Long topicId,
            @PathVariable("registrationId") Long registrationId,
            @Valid @RequestBody TopicCapacityRequest request,
            @AuthenticationPrincipal Jwt jwt) {

        if (jwt == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }
        String role = jwt.getClaimAsString("role");
        Long userId = null;
        if (!"ADMIN".equals(role)) {
            Object claim = jwt.getClaim("userId");
            try {
                userId = Long.valueOf(String.valueOf(claim));
            } catch (NumberFormatException ex) {
                throw new ResponseStatusException(
                        HttpStatus.FORBIDDEN, "JWT thiếu userId hợp lệ", ex);
            }
        }

        return service.synchronize(topicId, registrationId, request, userId, role);
    }
}
