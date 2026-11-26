package com.example.backend.user.mapper;

import com.example.backend.user.dto.UserCreateRequest;
import com.example.backend.user.dto.UserResponse;
import com.example.backend.user.entity.Role;
import com.example.backend.user.entity.User;

public final class UserMapper {

	private UserMapper() {
	}

	public static User toEntity(UserCreateRequest request, String email, String encodedPassword) {
		User user = new User();
		user.setName(request.name().trim());
		user.setEmail(email);
		user.setPassword(encodedPassword);
		user.setRole(Role.USER);
		return user;
	}

	public static UserResponse toResponse(User user) {
		return new UserResponse(
				user.getId(),
				user.getName(),
				user.getEmail(),
				user.getRole(),
				user.getCreatedAt());
	}

}
