package com.group.registration.client;

import com.group.registration.dto.StudentDTO;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "auth-service")
public interface StudentClient {

    @GetMapping("/api/auth/users/{id}")
    @CircuitBreaker(
            name = "authService",
            fallbackMethod = "fallbackStudent")
    @Retry(name = "authService")
    StudentDTO getStudentById(
            @PathVariable Long id,
            @RequestHeader(HttpHeaders.AUTHORIZATION)
            String authorization);

    default StudentDTO fallbackStudent(
            Long id,
            String authorization,
            Throwable throwable) {

        throw new RuntimeException(
                "Auth Service is temporarily unavailable. Please try again later.",
                throwable);
    }
    @GetMapping("/api/users/me")
    StudentDTO getCurrentStudent(
            @RequestHeader(HttpHeaders.AUTHORIZATION)
            String authorization
    );
}