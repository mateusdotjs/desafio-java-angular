package com.example.backend.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.example.backend.user.entity.Role;

import io.jsonwebtoken.JwtException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtServiceTest {

	private JwtService jwtService;

	@BeforeEach
	void setUp() {
		JwtProperties properties = new JwtProperties();
		properties.setSecret("change-me-use-at-least-32-characters-long");
		properties.setExpirationMs(3_600_000L);
		jwtService = new JwtService(properties);
	}

	@Test
	void generateToken_roundTrip_keepsIdEmailAndRole() {
		AuthPrincipal principal = AuthPrincipal.fromClaims(4L, "ana@mail.com", Role.ADMIN);

		AuthPrincipal parsed = jwtService.parseToken(jwtService.generateToken(principal));

		assertThat(parsed.id()).isEqualTo(4L);
		assertThat(parsed.email()).isEqualTo("ana@mail.com");
		assertThat(parsed.role()).isEqualTo(Role.ADMIN);
		assertThat(jwtService.getExpirationSeconds()).isEqualTo(3600L);
	}

	@Test
	void parseToken_tamperedToken_throwsJwtException() {
		String token = jwtService.generateToken(AuthPrincipal.fromClaims(4L, "ana@mail.com", Role.USER));
		char[] chars = token.toCharArray();
		int signatureStart = token.lastIndexOf('.') + 1;
		int index = signatureStart + (chars.length - signatureStart) / 2;
		chars[index] = chars[index] == 'a' ? 'b' : 'a';

		assertThatThrownBy(() -> jwtService.parseToken(new String(chars)))
				.isInstanceOf(JwtException.class);
	}

	@Test
	void parseToken_malformedToken_throwsJwtException() {
		assertThatThrownBy(() -> jwtService.parseToken("not-a-jwt"))
				.isInstanceOf(JwtException.class);
	}

}
