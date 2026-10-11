package com.graduation.notification.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(
            HttpSecurity http) throws Exception {

        return http
                .csrf(AbstractHttpConfigurer::disable)

                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS))

                .authorizeHttpRequests(auth -> auth

                        .requestMatchers(
                                HttpMethod.GET,
                                "/actuator/health")
                        .permitAll()

                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/notifications/me")
                        .authenticated()

                        .requestMatchers(
                                HttpMethod.PUT,
                                "/api/notifications/*/read")
                        .authenticated()

                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/notifications")
                        .hasRole("ADMIN")

                        .anyRequest()
                        .authenticated())

                .oauth2ResourceServer(
                        oauth2 ->
                                oauth2.jwt(jwt -> {}))

                .build();
    }
}