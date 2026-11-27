package com.example.backend.security;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.stereotype.Service;

import com.example.backend.user.entity.Role;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {

	private static final String EMAIL_CLAIM = "email";

	private static final String ROLE_CLAIM = "role";

	private final JwtProperties jwtProperties;

	private final SecretKey secretKey;

	public JwtService(JwtProperties jwtProperties) {
		this.jwtProperties = jwtProperties;
		this.secretKey = Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8));
	}

	public String generateToken(AuthPrincipal principal) {
		Instant now = Instant.now();
		Instant expiration = now.plusMillis(jwtProperties.getExpirationMs());
		return Jwts.builder()
				.subject(String.valueOf(principal.id()))
				.claim(EMAIL_CLAIM, principal.email())
				.claim(ROLE_CLAIM, principal.role().name())
				.issuedAt(Date.from(now))
				.expiration(Date.from(expiration))
				.signWith(secretKey)
				.compact();
	}

	public AuthPrincipal parseToken(String token) {
		try {
			Claims claims = Jwts.parser()
					.verifyWith(secretKey)
					.build()
					.parseSignedClaims(token)
					.getPayload();
			Long id = Long.parseLong(claims.getSubject());
			String email = claims.get(EMAIL_CLAIM, String.class);
			if (email == null) {
				email = "";
			}
			Role role = Role.valueOf(claims.get(ROLE_CLAIM, String.class));
			return AuthPrincipal.fromClaims(id, email, role);
		}
		catch (JwtException | IllegalArgumentException exception) {
			throw new JwtException("Invalid JWT token", exception);
		}
	}

	public long getExpirationSeconds() {
		return jwtProperties.getExpirationMs() / 1000L;
	}

}