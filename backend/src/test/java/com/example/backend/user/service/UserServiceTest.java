package com.example.backend.user.service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.example.backend.common.exception.EmailAlreadyExistsException;
import com.example.backend.common.exception.ForbiddenAccessException;
import com.example.backend.common.exception.InvalidSortException;
import com.example.backend.common.exception.ResourceNotFoundException;
import com.example.backend.support.SecurityTestSupport;
import com.example.backend.user.dto.UserCreateRequest;
import com.example.backend.user.dto.UserResponse;
import com.example.backend.user.dto.UserUpdateRequest;
import com.example.backend.user.entity.Role;
import com.example.backend.user.entity.User;
import com.example.backend.user.repository.UserRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

	private static final Instant CREATED_AT = Instant.parse("2026-01-01T00:00:00Z");

	@Mock
	private UserRepository userRepository;

	@Mock
	private PasswordEncoder passwordEncoder;

	@InjectMocks
	private UserService userService;

	@AfterEach
	void clearSecurity() {
		SecurityTestSupport.clear();
	}

	@Test
	void create_newEmail_normalizesEmailAndStoresEncodedPassword() {
		when(passwordEncoder.encode("password1")).thenReturn("encoded-password");
		when(userRepository.saveAndFlush(any(User.class))).thenAnswer(invocation -> {
			User user = invocation.getArgument(0);
			user.setId(10L);
			user.setCreatedAt(CREATED_AT);
			return user;
		});

		UserResponse response = userService.create(new UserCreateRequest("  Ana  ", "  User@Mail.COM ", "password1"));

		assertThat(response.id()).isEqualTo(10L);
		assertThat(response.name()).isEqualTo("Ana");
		assertThat(response.email()).isEqualTo("user@mail.com");
		assertThat(response.role()).isEqualTo(Role.USER);
		ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
		verify(userRepository).saveAndFlush(captor.capture());
		assertThat(captor.getValue().getPassword()).isEqualTo("encoded-password");
	}

	@Test
	void create_existingEmail_throwsEmailAlreadyExists() {
		when(userRepository.existsByEmail("user@mail.com")).thenReturn(true);

		assertThatThrownBy(() -> userService.create(createRequest()))
				.isInstanceOf(EmailAlreadyExistsException.class);
		verify(userRepository, never()).saveAndFlush(any());
	}

	@Test
	void create_concurrentDuplicate_throwsEmailAlreadyExists() {
		when(passwordEncoder.encode("password1")).thenReturn("encoded-password");
		when(userRepository.saveAndFlush(any(User.class))).thenThrow(new DataIntegrityViolationException("duplicate"));

		assertThatThrownBy(() -> userService.create(createRequest()))
				.isInstanceOf(EmailAlreadyExistsException.class);
	}

	@Test
	void createAsAdmin_withoutAdmin_throwsForbidden() {
		assertThatThrownBy(() -> userService.createAsAdmin(createRequest()))
				.isInstanceOf(ForbiddenAccessException.class);
	}

	@Test
	void createAsAdmin_asUser_throwsForbidden() {
		SecurityTestSupport.authenticate(2L, "user@mail.com", Role.USER);

		assertThatThrownBy(() -> userService.createAsAdmin(createRequest()))
				.isInstanceOf(ForbiddenAccessException.class);
	}

	@Test
	void createAsAdmin_asAdmin_createsUser() {
		SecurityTestSupport.authenticate(1L, "admin@mail.com", Role.ADMIN);
		when(passwordEncoder.encode("password1")).thenReturn("encoded-password");
		when(userRepository.saveAndFlush(any(User.class))).thenAnswer(invocation -> {
			User user = invocation.getArgument(0);
			user.setId(11L);
			user.setCreatedAt(CREATED_AT);
			return user;
		});

		UserResponse response = userService.createAsAdmin(createRequest());

		assertThat(response.email()).isEqualTo("user@mail.com");
		assertThat(response.role()).isEqualTo(Role.USER);
	}

	@Test
	void list_allowedSorts_returnPage() {
		SecurityTestSupport.authenticate(1L, "admin@mail.com", Role.ADMIN);
		User user = persistedUser(10L, "user@mail.com");
		for (String field : List.of("name", "email", "createdAt")) {
			Pageable pageable = PageRequest.of(0, 20, Sort.by(field));
			when(userRepository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(user), pageable, 1));

			Page<UserResponse> page = userService.list(pageable);

			assertThat(page.getContent()).extracting(UserResponse::email).containsExactly("user@mail.com");
		}
	}

	@Test
	void list_asUser_throwsForbidden() {
		SecurityTestSupport.authenticate(2L, "user@mail.com", Role.USER);

		assertThatThrownBy(() -> userService.list(PageRequest.of(0, 20)))
				.isInstanceOf(ForbiddenAccessException.class);
	}

	@Test
	void list_disallowedSort_throwsInvalidSort() {
		SecurityTestSupport.authenticate(1L, "admin@mail.com", Role.ADMIN);
		Pageable pageable = PageRequest.of(0, 20, Sort.by("password"));

		assertThatThrownBy(() -> userService.list(pageable))
				.isInstanceOf(InvalidSortException.class);
	}

	@Test
	void findById_asOwner_returnsUser() {
		when(userRepository.findById(10L)).thenReturn(Optional.of(persistedUser(10L, "user@mail.com")));
		SecurityTestSupport.authenticate(10L, "user@mail.com", Role.USER);

		UserResponse response = userService.findById(10L);

		assertThat(response.email()).isEqualTo("user@mail.com");
	}

	@Test
	void findById_asAdmin_returnsOtherUser() {
		when(userRepository.findById(10L)).thenReturn(Optional.of(persistedUser(10L, "user@mail.com")));
		SecurityTestSupport.authenticate(1L, "admin@mail.com", Role.ADMIN);

		UserResponse response = userService.findById(10L);

		assertThat(response.id()).isEqualTo(10L);
	}

	@Test
	void findById_asOtherUser_throwsForbidden() {
		SecurityTestSupport.authenticate(11L, "other@mail.com", Role.USER);

		assertThatThrownBy(() -> userService.findById(10L))
				.isInstanceOf(ForbiddenAccessException.class);
	}

	@Test
	void findById_missingUser_throwsNotFound() {
		when(userRepository.findById(10L)).thenReturn(Optional.empty());
		SecurityTestSupport.authenticate(1L, "admin@mail.com", Role.ADMIN);

		assertThatThrownBy(() -> userService.findById(10L))
				.isInstanceOf(ResourceNotFoundException.class)
				.hasMessage("User not found");
	}

	@Test
	void update_asOwner_changesNameEmailAndPassword() {
		User user = persistedUser(10L, "user@mail.com");
		when(userRepository.findById(10L)).thenReturn(Optional.of(user));
		when(passwordEncoder.encode("newpassword")).thenReturn("new-hash");
		when(userRepository.saveAndFlush(user)).thenReturn(user);
		SecurityTestSupport.authenticate(10L, "user@mail.com", Role.USER);

		UserResponse response = userService.update(
				10L,
				new UserUpdateRequest("  Ana Maria  ", "  Ana@Mail.com ", "newpassword"));

		assertThat(response.name()).isEqualTo("Ana Maria");
		assertThat(response.email()).isEqualTo("ana@mail.com");
		assertThat(user.getPassword()).isEqualTo("new-hash");
	}

	@Test
	void update_asAdmin_updatesAnotherUser() {
		User user = persistedUser(10L, "user@mail.com");
		when(userRepository.findById(10L)).thenReturn(Optional.of(user));
		when(userRepository.saveAndFlush(user)).thenReturn(user);
		SecurityTestSupport.authenticate(1L, "admin@mail.com", Role.ADMIN);

		UserResponse response = userService.update(10L, new UserUpdateRequest("Ana", "user@mail.com", null));

		assertThat(response.name()).isEqualTo("Ana");
		assertThat(user.getPassword()).isEqualTo("encoded");
	}

	@Test
	void update_blankPassword_keepsCurrentPassword() {
		User user = persistedUser(10L, "user@mail.com");
		when(userRepository.findById(10L)).thenReturn(Optional.of(user));
		when(userRepository.saveAndFlush(user)).thenReturn(user);
		SecurityTestSupport.authenticate(10L, "user@mail.com", Role.USER);

		userService.update(10L, new UserUpdateRequest("Ana", "user@mail.com", "   "));

		assertThat(user.getPassword()).isEqualTo("encoded");
		verify(passwordEncoder, never()).encode(any());
	}

	@Test
	void update_existingEmail_throwsEmailAlreadyExists() {
		when(userRepository.findById(10L)).thenReturn(Optional.of(persistedUser(10L, "user@mail.com")));
		when(userRepository.existsByEmailAndIdNot("taken@mail.com", 10L)).thenReturn(true);
		SecurityTestSupport.authenticate(10L, "user@mail.com", Role.USER);

		assertThatThrownBy(() -> userService.update(10L, new UserUpdateRequest("Ana", "taken@mail.com", null)))
				.isInstanceOf(EmailAlreadyExistsException.class);
		verify(userRepository, never()).saveAndFlush(any());
	}

	@Test
	void update_concurrentDuplicate_throwsEmailAlreadyExists() {
		User user = persistedUser(10L, "user@mail.com");
		when(userRepository.findById(10L)).thenReturn(Optional.of(user));
		when(userRepository.saveAndFlush(user)).thenThrow(new DataIntegrityViolationException("duplicate"));
		SecurityTestSupport.authenticate(10L, "user@mail.com", Role.USER);

		assertThatThrownBy(() -> userService.update(10L, new UserUpdateRequest("Ana", "user@mail.com", null)))
				.isInstanceOf(EmailAlreadyExistsException.class);
	}

	@Test
	void update_asOtherUser_throwsForbidden() {
		SecurityTestSupport.authenticate(11L, "other@mail.com", Role.USER);

		assertThatThrownBy(() -> userService.update(10L, new UserUpdateRequest("Ana", "user@mail.com", null)))
				.isInstanceOf(ForbiddenAccessException.class);
	}

	@Test
	void update_missingUser_throwsNotFound() {
		when(userRepository.findById(10L)).thenReturn(Optional.empty());
		SecurityTestSupport.authenticate(1L, "admin@mail.com", Role.ADMIN);

		assertThatThrownBy(() -> userService.update(10L, new UserUpdateRequest("Ana", "user@mail.com", null)))
				.isInstanceOf(ResourceNotFoundException.class)
				.hasMessage("User not found");
	}

	@Test
	void delete_asOwner_removesUser() {
		when(userRepository.existsById(10L)).thenReturn(true);
		SecurityTestSupport.authenticate(10L, "user@mail.com", Role.USER);

		userService.delete(10L);

		verify(userRepository).deleteById(10L);
	}

	@Test
	void delete_asAdmin_removesAnotherUser() {
		when(userRepository.existsById(10L)).thenReturn(true);
		SecurityTestSupport.authenticate(1L, "admin@mail.com", Role.ADMIN);

		userService.delete(10L);

		verify(userRepository).deleteById(10L);
	}

	@Test
	void delete_missingUser_throwsNotFound() {
		when(userRepository.existsById(10L)).thenReturn(false);
		SecurityTestSupport.authenticate(10L, "user@mail.com", Role.USER);

		assertThatThrownBy(() -> userService.delete(10L))
				.isInstanceOf(ResourceNotFoundException.class)
				.hasMessage("User not found");
		verify(userRepository, never()).deleteById(any());
	}

	@Test
	void delete_asOtherUser_throwsForbidden() {
		SecurityTestSupport.authenticate(11L, "other@mail.com", Role.USER);

		assertThatThrownBy(() -> userService.delete(10L))
				.isInstanceOf(ForbiddenAccessException.class);
	}

	private UserCreateRequest createRequest() {
		return new UserCreateRequest("Ana", "user@mail.com", "password1");
	}

	private User persistedUser(Long id, String email) {
		User user = new User();
		user.setId(id);
		user.setName("Ana");
		user.setEmail(email);
		user.setPassword("encoded");
		user.setRole(Role.USER);
		user.setCreatedAt(CREATED_AT);
		return user;
	}

}
