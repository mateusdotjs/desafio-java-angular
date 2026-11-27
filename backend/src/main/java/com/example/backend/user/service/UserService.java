package com.example.backend.user.service;

import java.util.Locale;
import java.util.Set;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.backend.common.exception.EmailAlreadyExistsException;
import com.example.backend.common.exception.ResourceNotFoundException;
import com.example.backend.common.pagination.SortValidator;
import com.example.backend.security.SecurityUtils;
import com.example.backend.user.dto.UserCreateRequest;
import com.example.backend.user.dto.UserResponse;
import com.example.backend.user.dto.UserUpdateRequest;
import com.example.backend.user.entity.User;
import com.example.backend.user.mapper.UserMapper;
import com.example.backend.user.repository.UserRepository;

@Service
@Transactional(readOnly = true)
public class UserService {

	private static final Set<String> ALLOWED_SORTS = Set.of("name", "email", "createdAt");

	private final UserRepository userRepository;

	private final PasswordEncoder passwordEncoder;

	public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
	}

	@Transactional
	public UserResponse createAsAdmin(UserCreateRequest request) {
		SecurityUtils.assertAdmin();
		return create(request);
	}

	@Transactional
	public UserResponse create(UserCreateRequest request) {
		String email = normalizeEmail(request.email());
		if (userRepository.existsByEmail(email)) {
			throw new EmailAlreadyExistsException();
		}

		User user = UserMapper.toEntity(request, email, passwordEncoder.encode(request.password()));
		try {
			return UserMapper.toResponse(userRepository.saveAndFlush(user));
		}
		catch (DataIntegrityViolationException exception) {
			throw new EmailAlreadyExistsException();
		}
	}

	public Page<UserResponse> list(Pageable pageable) {
		SecurityUtils.assertAdmin();
		SortValidator.validate(pageable, ALLOWED_SORTS);
		return userRepository.findAll(pageable).map(UserMapper::toResponse);
	}

	public UserResponse findById(Long id) {
		SecurityUtils.assertAdminOrOwner(id);
		return UserMapper.toResponse(getUser(id));
	}

	public UserResponse findByIdForAuth(Long id) {
		return UserMapper.toResponse(getUser(id));
	}

	@Transactional
	public UserResponse update(Long id, UserUpdateRequest request) {
		SecurityUtils.assertAdminOrOwner(id);
		User user = getUser(id);
		String email = normalizeEmail(request.email());
		if (userRepository.existsByEmailAndIdNot(email, id)) {
			throw new EmailAlreadyExistsException();
		}

		user.setName(request.name().trim());
		user.setEmail(email);
		if (request.password() != null && !request.password().isBlank()) {
			user.setPassword(passwordEncoder.encode(request.password()));
		}

		try {
			return UserMapper.toResponse(userRepository.saveAndFlush(user));
		}
		catch (DataIntegrityViolationException exception) {
			throw new EmailAlreadyExistsException();
		}
	}

	@Transactional
	public void delete(Long id) {
		SecurityUtils.assertAdminOrOwner(id);
		if (!userRepository.existsById(id)) {
			throw new ResourceNotFoundException("User not found");
		}
		userRepository.deleteById(id);
	}

	private User getUser(Long id) {
		return userRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("User not found"));
	}

	private String normalizeEmail(String email) {
		return email.trim().toLowerCase(Locale.ROOT);
	}

}
