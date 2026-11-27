package com.example.backend.auth.service;

import java.util.Locale;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.backend.auth.dto.AuthResponse;
import com.example.backend.auth.dto.LoginRequest;
import com.example.backend.common.exception.InvalidCredentialsException;
import com.example.backend.security.AuthPrincipal;
import com.example.backend.security.JwtService;
import com.example.backend.user.dto.UserCreateRequest;
import com.example.backend.user.dto.UserResponse;
import com.example.backend.user.service.UserService;

@Service
public class AuthService {

	private static final String TOKEN_TYPE = "Bearer";

	private final AuthenticationManager authenticationManager;

	private final JwtService jwtService;

	private final UserService userService;

	public AuthService(AuthenticationManager authenticationManager, JwtService jwtService, UserService userService) {
		this.authenticationManager = authenticationManager;
		this.jwtService = jwtService;
		this.userService = userService;
	}

	public AuthResponse login(LoginRequest request) {
		String email = normalizeEmail(request.email());
		try {
			var authentication = authenticationManager.authenticate(
					new UsernamePasswordAuthenticationToken(email, request.password()));
			AuthPrincipal principal = (AuthPrincipal) authentication.getPrincipal();
			return toAuthResponse(principal);
		}
		catch (AuthenticationException exception) {
			throw new InvalidCredentialsException();
		}
	}

	@Transactional
	public AuthResponse register(UserCreateRequest request) {
		UserResponse user = userService.create(request);
		AuthPrincipal principal = AuthPrincipal.fromClaims(user.id(), user.email(), user.role());
		return toAuthResponse(principal);
	}

	private AuthResponse toAuthResponse(AuthPrincipal principal) {
		UserResponse user = userService.findByIdForAuth(principal.id());
		String token = jwtService.generateToken(principal);
		return new AuthResponse(token, TOKEN_TYPE, jwtService.getExpirationSeconds(), user);
	}

	private String normalizeEmail(String email) {
		return email.trim().toLowerCase(Locale.ROOT);
	}

}