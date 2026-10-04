package com.graduation.auth.service;

import com.graduation.auth.entity.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;
import java.time.Instant;

@Service
public class JwtService {
	private final JwtEncoder encoder;
	private final String issuer;
	private final long expiration;

	public JwtService(JwtEncoder e, @Value("${jwt.issuer}") String i, @Value("${jwt.expiration-seconds}") long x) {
		encoder = e;
		issuer = i;
		expiration = x;
	}

	public String generateToken(User u) {
		Instant now = Instant.now();
		var claims = JwtClaimsSet.builder().issuer(issuer).issuedAt(now).expiresAt(now.plusSeconds(expiration))
				.subject(u.getUserCode()).claim("userId", u.getId()).claim("role", u.getRole().name())
				.claim("fullName", u.getFullName()).build();
		var header = JwsHeader.with(MacAlgorithm.HS256).type("JWT").build();
		return encoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
	}

	public long getExpirationSeconds() {
		return expiration;
	}
}
