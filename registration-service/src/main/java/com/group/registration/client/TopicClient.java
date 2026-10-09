package com.group.registration.client;

import com.group.registration.dto.TopicDTO;
import com.group.registration.dto.TopicCapacityRequest;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PutMapping;

@FeignClient(name = "topic-service")
public interface TopicClient {

    // Không dùng fallback trả dữ liệu giả cho thao tác thay đổi số chỗ.
    @PutMapping("/api/topics/{topicId}/registrations/{registrationId}/capacity")
    TopicDTO synchronizeCapacity(
            @PathVariable("topicId") Long topicId,
            @PathVariable("registrationId") Long registrationId,
            @RequestBody TopicCapacityRequest request,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization);

    @GetMapping("/api/topics/{id}")
    @CircuitBreaker(
            name = "topicService",
            fallbackMethod = "fallbackTopic")
    @Retry(name = "topicService")
    TopicDTO getTopicById(
            @PathVariable Long id,
            @RequestHeader(HttpHeaders.AUTHORIZATION)
            String authorization);

    default TopicDTO fallbackTopic(
            Long id,
            String authorization,
            Throwable throwable) {

        throw new RuntimeException(
                "Topic Service is temporarily unavailable. Please try again later.",
                throwable);
    }
}
