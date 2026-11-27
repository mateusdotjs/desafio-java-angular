package com.example.backend.user.repository;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;

import com.example.backend.support.AbstractRepositoryTest;
import com.example.backend.user.entity.Role;
import com.example.backend.user.entity.User;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UserRepositoryTest extends AbstractRepositoryTest {

	@Autowired
	private UserRepository userRepository;

	@Test
	void existsByEmail_matchesStoredEmail() {
		persist("Ana", "ana@mail.com");

		assertThat(userRepository.existsByEmail("ana@mail.com")).isTrue();
		assertThat(userRepository.existsByEmail("other@mail.com")).isFalse();
	}

	@Test
	void existsByEmailAndIdNot_ignoresTheSameUser() {
		User ana = persist("Ana", "ana@mail.com");
		User bia = persist("Bia", "bia@mail.com");

		assertThat(userRepository.existsByEmailAndIdNot("ana@mail.com", bia.getId())).isTrue();
		assertThat(userRepository.existsByEmailAndIdNot("ana@mail.com", ana.getId())).isFalse();
		assertThat(userRepository.existsByEmailAndIdNot("missing@mail.com", ana.getId())).isFalse();
	}

	@Test
	void findByEmail_returnsMatchingUser() {
		persist("Ana", "ana@mail.com");

		Optional<User> found = userRepository.findByEmail("ana@mail.com");

		assertThat(found).isPresent();
		assertThat(found.orElseThrow().getName()).isEqualTo("Ana");
		assertThat(userRepository.findByEmail("missing@mail.com")).isEmpty();
	}

	@Test
	void save_duplicateEmail_throwsDataIntegrityViolation() {
		persist("Ana", "ana@mail.com");

		assertThatThrownBy(() -> persist("Bia", "ana@mail.com"))
				.isInstanceOf(DataIntegrityViolationException.class);
	}

	private User persist(String name, String email) {
		User user = new User();
		user.setName(name);
		user.setEmail(email);
		user.setPassword("encoded");
		user.setRole(Role.USER);
		return userRepository.saveAndFlush(user);
	}

}
