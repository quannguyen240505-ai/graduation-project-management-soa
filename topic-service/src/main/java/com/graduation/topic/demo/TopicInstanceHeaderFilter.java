package com.graduation.topic.demo;

import java.io.IOException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Cho biết instance thực sự xử lý request khi demo failover.
 * Filter chỉ được đăng ký ở profile demo; giữ nguyên body và các rule bảo mật.
 */
@Component
@Profile("topic-failover-demo")
@Order(Ordered.HIGHEST_PRECEDENCE)
public class TopicInstanceHeaderFilter extends OncePerRequestFilter {

    private final String instanceId;

    /** @param instanceId ID đăng ký Eureka, ví dụ topic-service:8082 */
    public TopicInstanceHeaderFilter(
            @Value("${eureka.instance.instance-id}") String instanceId) {
        this.instanceId = instanceId;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        response.setHeader("X-Topic-Instance", instanceId);
        filterChain.doFilter(request, response);
    }
}
