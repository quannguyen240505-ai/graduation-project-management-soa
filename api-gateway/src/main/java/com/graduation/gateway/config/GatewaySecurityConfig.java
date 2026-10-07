package com.graduation.gateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.web.server.SecurityWebFilterChain;
import reactor.core.publisher.Mono;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

@Configuration
public class GatewaySecurityConfig {
	@Bean
	SecretKey jwtSecretKey(@Value("${jwt.secret}") String secret) {
		byte[] b = secret.getBytes(StandardCharsets.UTF_8);
		if (b.length < 32)
			throw new IllegalStateException("JWT_SECRET phải có ít nhất 32 bytes để dùng HS256");
		return new SecretKeySpec(b, "HmacSHA256");
	}

	@Bean
	ReactiveJwtDecoder jwtDecoder(SecretKey key, @Value("${jwt.issuer}") String issuer) {
		var d = NimbusReactiveJwtDecoder.withSecretKey(key).macAlgorithm(MacAlgorithm.HS256).build();
		d.setJwtValidator(JwtValidators.createDefaultWithIssuer(issuer));
		return d;
	}

	@Bean
	SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http,
			Converter<Jwt, Mono<AbstractAuthenticationToken>> c) {

		return http.csrf(ServerHttpSecurity.CsrfSpec::disable)

				.authorizeExchange(e -> e

						// Public
						.pathMatchers(HttpMethod.POST, "/api/auth/register", "/api/auth/login").permitAll()

						.pathMatchers(HttpMethod.GET, "/actuator/health").permitAll()

						// Authenticated user APIs
						.pathMatchers("/api/users/**").authenticated()

						// API nội bộ để các service tra cứu User
						.pathMatchers(HttpMethod.GET, "/api/auth/users/**").authenticated()

						// Topic
						.pathMatchers(HttpMethod.GET, "/api/topics/**").hasAnyRole("STUDENT", "LECTURER", "ADMIN")

						.pathMatchers(HttpMethod.POST, "/api/topics/**").hasAnyRole("LECTURER", "ADMIN")

						.pathMatchers(HttpMethod.PUT, "/api/topics/**").hasAnyRole("LECTURER", "ADMIN")

						.pathMatchers(HttpMethod.DELETE, "/api/topics/**").hasAnyRole("LECTURER", "ADMIN")

						// Registration + Notification
						.pathMatchers("/api/registrations/**", "/api/notifications/**").authenticated()

						.anyExchange().authenticated())

				.oauth2ResourceServer(o -> o.jwt(j -> j.jwtAuthenticationConverter(c)))

				.build();
	}
}
