package com.example.backend.support;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import com.example.backend.security.AuthPrincipal;
import com.example.backend.user.entity.Role;

public final class SecurityTestSupport {

	private SecurityTestSupport() {
	}

	public static void authenticate(Long id, String email, Role role) {
		AuthPrincipal principal = AuthPrincipal.fromClaims(id, email, role);
		UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
				principal,
				null,
				principal.getAuthorities());
		SecurityContextHolder.getContext().setAuthentication(authentication);
	}

	public static void clear() {
		SecurityContextHolder.clearContext();
	}

}
