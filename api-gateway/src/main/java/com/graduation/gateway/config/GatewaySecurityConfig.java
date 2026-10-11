package com.graduation.gateway.config;

import java.nio.charset.StandardCharsets;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.web.server.SecurityWebFilterChain;

import reactor.core.publisher.Mono;

@Configuration
public class GatewaySecurityConfig {

    @Bean
    SecretKey jwtSecretKey(
            @Value("${jwt.secret}") String secret) {

        byte[] bytes =
                secret.getBytes(StandardCharsets.UTF_8);

        if (bytes.length < 32) {
            throw new IllegalStateException(
                    "JWT_SECRET phải có ít nhất 32 bytes để dùng HS256");
        }

        return new SecretKeySpec(
                bytes,
                "HmacSHA256");
    }

    @Bean
    ReactiveJwtDecoder jwtDecoder(
            SecretKey key,
            @Value("${jwt.issuer}") String issuer) {

        NimbusReactiveJwtDecoder decoder =
                NimbusReactiveJwtDecoder
                        .withSecretKey(key)
                        .macAlgorithm(MacAlgorithm.HS256)
                        .build();

        decoder.setJwtValidator(
                JwtValidators.createDefaultWithIssuer(issuer));

        return decoder;
    }

    @Bean
    SecurityWebFilterChain securityWebFilterChain(
            ServerHttpSecurity http,
            Converter<Jwt, Mono<AbstractAuthenticationToken>>
                    jwtAuthenticationConverter) {

        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)

                .authorizeExchange(exchange -> exchange

                        // =========================
                        // Public
                        // =========================

                        .pathMatchers(
                                HttpMethod.POST,
                                "/api/auth/register",
                                "/api/auth/login")
                        .permitAll()

                        .pathMatchers(
                                HttpMethod.GET,
                                "/actuator/health")
                        .permitAll()

                        // =========================
                        // User
                        // =========================

                        .pathMatchers(
                                "/api/users/**")
                        .authenticated()

                        // API phục vụ các service tra cứu User
                        .pathMatchers(
                                HttpMethod.GET,
                                "/api/auth/users/**")
                        .authenticated()

                        // =========================
                        // Topic
                        // =========================

                        /*
                         * API capacity chỉ dùng cho giao tiếp
                         * giữa Registration Service và Topic Service.
                         *
                         * Client bên ngoài không được gọi endpoint này
                         * thông qua API Gateway.
                         */
                        .pathMatchers(
                                "/api/topics/*/registrations/*/capacity")
                        .denyAll()

                        .pathMatchers(
                                HttpMethod.GET,
                                "/api/topics/**")
                        .hasAnyRole(
                                "STUDENT",
                                "LECTURER",
                                "ADMIN")

                        .pathMatchers(
                                HttpMethod.POST,
                                "/api/topics/**")
                        .hasAnyRole(
                                "LECTURER",
                                "ADMIN")

                        .pathMatchers(
                                HttpMethod.PUT,
                                "/api/topics/**")
                        .hasAnyRole(
                                "LECTURER",
                                "ADMIN")

                        .pathMatchers(
                                HttpMethod.DELETE,
                                "/api/topics/**")
                        .hasAnyRole(
                                "LECTURER",
                                "ADMIN")

                        // =========================
                        // Registration
                        // =========================

                        /*
                         * Chỉ STUDENT được tạo đăng ký.
                         * Registration Service sẽ kiểm tra userId
                         * trong JWT và chống đăng ký thay người khác.
                         */
                        .pathMatchers(
                                HttpMethod.POST,
                                "/api/registrations")
                        .hasRole("STUDENT")

                        /*
                         * STUDENT được phép gọi GET nhưng
                         * Registration Service chỉ trả đăng ký
                         * thuộc chính sinh viên đang đăng nhập.
                         *
                         * LECTURER và ADMIN được phép xem
                         * danh sách đăng ký theo quyền backend.
                         */
                        .pathMatchers(
                                HttpMethod.GET,
                                "/api/registrations/**")
                        .hasAnyRole(
                                "STUDENT",
                                "LECTURER",
                                "ADMIN")

                        // Chỉ Lecturer/Admin được duyệt hoặc từ chối
                        .pathMatchers(
                                HttpMethod.PUT,
                                "/api/registrations/*/status")
                        .hasAnyRole(
                                "LECTURER",
                                "ADMIN")

                        // Chỉ Lecturer/Admin được xóa đăng ký
                        .pathMatchers(
                                HttpMethod.DELETE,
                                "/api/registrations/**")
                        .hasAnyRole(
                                "LECTURER",
                                "ADMIN")

                        // =========================
                        // Notification
                        // =========================

                        /*
                         * Người dùng đã đăng nhập
                         * được phép xem danh sách notification.
                         */
                        .pathMatchers(
                                HttpMethod.GET,
                                "/api/notifications")
                        .authenticated()

                        /*
                         * Người dùng đã đăng nhập
                         * được phép xem notification theo user.
                         *
                         * Ownership nên được kiểm tra tiếp
                         * ở Notification Service.
                         */
                        .pathMatchers(
                                HttpMethod.GET,
                                "/api/notifications/user/**")
                        .authenticated()

                        /*
                         * Người dùng đã đăng nhập
                         * được phép đánh dấu notification đã đọc.
                         */
                        .pathMatchers(
                                HttpMethod.PUT,
                                "/api/notifications/*/read")
                        .authenticated()

                        // =========================
                        // Các request còn lại
                        // =========================

                        .anyExchange()
                        .authenticated())

                .oauth2ResourceServer(
                        oauth2 -> oauth2.jwt(
                                jwt -> jwt.jwtAuthenticationConverter(
                                        jwtAuthenticationConverter)))

                .build();
    }
}