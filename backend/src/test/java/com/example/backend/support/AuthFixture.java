package com.example.backend.support;

import org.springframework.security.crypto.password.PasswordEncoder;

import com.example.backend.security.AuthPrincipal;
import com.example.backend.security.JwtService;
import com.example.backend.user.entity.Role;
import com.example.backend.user.entity.User;
import com.example.backend.user.repository.UserRepository;

public class AuthFixture {

	public static final String RAW_PASSWORD = "password1";

	private final UserRepository userRepository;

	private final PasswordEncoder passwordEncoder;

	private final JwtService jwtService;

	public AuthFixture(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
		this.jwtService = jwtService;
	}

	public User createUser(String name, String email, Role role) {
		User user = new User();
		user.setName(name);
		user.setEmail(email);
		user.setPassword(passwordEncoder.encode(RAW_PASSWORD));
		user.setRole(role);
		return userRepository.saveAndFlush(user);
	}

	public String bearer(User user) {
		return "Bearer " + jwtService.generateToken(AuthPrincipal.from(user));
	}

}
