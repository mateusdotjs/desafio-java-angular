package com.example.backend.security;

import java.io.IOException;
import java.time.Instant;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import com.example.backend.common.response.ErrorResponse;

import tools.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class SecurityExceptionHandlers implements AuthenticationEntryPoint, AccessDeniedHandler {

	private final ObjectMapper objectMapper;

	public SecurityExceptionHandlers(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}

	@Override
	public void commence(
			HttpServletRequest request,
			HttpServletResponse response,
			AuthenticationException authException) throws IOException {
		writeError(response, request, HttpStatus.UNAUTHORIZED, "Authentication required");
	}

	@Override
	public void handle(
			HttpServletRequest request,
			HttpServletResponse response,
			AccessDeniedException accessDeniedException) throws IOException {
		writeError(response, request, HttpStatus.FORBIDDEN, "Access denied");
	}

	private void writeError(HttpServletResponse response, HttpServletRequest request, HttpStatus status, String message)
			throws IOException {
		ErrorResponse body = new ErrorResponse(
				Instant.now(),
				status.value(),
				status.getReasonPhrase(),
				message,
				request.getRequestURI(),
				List.of());
		response.setStatus(status.value());
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		objectMapper.writeValue(response.getOutputStream(), body);
	}

}