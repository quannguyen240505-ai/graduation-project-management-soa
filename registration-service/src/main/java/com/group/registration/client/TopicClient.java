package com.group.registration.client;

import com.group.registration.dto.TopicDTO;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "topic-service")
public interface TopicClient {

    @GetMapping("/api/topics/{id}")
    @CircuitBreaker(name = "topicService", fallbackMethod = "fallbackTopic")
    @Retry(name = "topicService")
    @TimeLimiter(name = "topicService")
    TopicDTO getTopicById(@PathVariable("id") Long id);

    default TopicDTO fallbackTopic(Long id, Throwable t) {
        throw new RuntimeException("Topic Service is temporarily unavailable. Please try again later.");
    }
}