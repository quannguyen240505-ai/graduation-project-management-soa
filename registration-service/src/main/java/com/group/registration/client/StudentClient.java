package com.group.registration.client;

import com.group.registration.dto.StudentDTO;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "student-service")
public interface StudentClient {

    @GetMapping("/api/students/{id}")
    @CircuitBreaker(name = "studentService", fallbackMethod = "fallbackStudent")
    @Retry(name = "studentService")
    @TimeLimiter(name = "studentService")
    StudentDTO getStudentById(@PathVariable("id") Long id);

    // Phương thức Fallback: Trả về lỗi ngay lập tức khi Student Service sập
    default StudentDTO fallbackStudent(Long id, Throwable t) {
        throw new RuntimeException("Student Service is temporarily unavailable. Please try again later.");
    }
}