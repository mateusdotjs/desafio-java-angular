package com.example.backend.user;

import java.util.Locale;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.example.backend.user.entity.Role;
import com.example.backend.user.entity.User;
import com.example.backend.user.repository.UserRepository;

@Component
public class AdminInitializer implements ApplicationRunner {

	private final AdminProperties properties;

	private final UserRepository userRepository;

	private final PasswordEncoder passwordEncoder;

	public AdminInitializer(AdminProperties properties, UserRepository userRepository, PasswordEncoder passwordEncoder) {
		this.properties = properties;
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
	}

	@Override
	@Transactional
	public void run(ApplicationArguments args) {
		if (!properties.isEnabled()) {
			return;
		}

		String email = properties.getEmail().trim().toLowerCase(Locale.ROOT);
		if (userRepository.existsByEmail(email)) {
			return;
		}

		User admin = new User();
		admin.setName(properties.getName().trim());
		admin.setEmail(email);
		admin.setPassword(passwordEncoder.encode(properties.getPassword()));
		admin.setRole(Role.ADMIN);
		userRepository.save(admin);
	}

}
