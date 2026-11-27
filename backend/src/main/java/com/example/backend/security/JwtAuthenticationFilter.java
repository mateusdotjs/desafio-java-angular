package com.example.backend.security;

import java.io.IOException;

import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	private final JwtService jwtService;

	public JwtAuthenticationFilter(JwtService jwtService) {
		this.jwtService = jwtService;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		String header = request.getHeader(HttpHeaders.AUTHORIZATION);
		if (header != null && header.startsWith("Bearer ")) {
			String token = header.substring(7).trim();
			if (!token.isEmpty()) {
				try {
					AuthPrincipal principal = jwtService.parseToken(token);
					UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
							principal,
							null,
							principal.getAuthorities());
					SecurityContextHolder.getContext().setAuthentication(authentication);
				}
				catch (JwtException exception) {
					SecurityContextHolder.clearContext();
				}
			}
		}
		filterChain.doFilter(request, response);
	}

}