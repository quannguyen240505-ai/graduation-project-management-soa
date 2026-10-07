package com.group.registration.client;

import com.group.registration.dto.TopicDTO;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "topic-service")
public interface TopicClient {

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