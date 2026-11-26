package com.example.backend.user.dto;

import java.time.Instant;

import com.example.backend.user.entity.Role;

public record UserResponse(
		Long id,
		String name,
		String email,
		Role role,
		Instant createdAt) {
}
