package com.example.backend.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.example.backend.common.exception.ForbiddenAccessException;
import com.example.backend.user.entity.Role;

public final class SecurityUtils {

	private SecurityUtils() {
	}

	public static AuthPrincipal requirePrincipal() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null || !(authentication.getPrincipal() instanceof AuthPrincipal principal)) {
			throw new ForbiddenAccessException();
		}
		return principal;
	}

	public static void assertAdmin() {
		if (requirePrincipal().role() != Role.ADMIN) {
			throw new ForbiddenAccessException();
		}
	}

	public static void assertAdminOrOwner(Long userId) {
		AuthPrincipal principal = requirePrincipal();
		if (principal.role() == Role.ADMIN || principal.id().equals(userId)) {
			return;
		}
		throw new ForbiddenAccessException();
	}

}