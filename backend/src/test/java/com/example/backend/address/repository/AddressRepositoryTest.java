package com.example.backend.address.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import com.example.backend.address.entity.Address;
import com.example.backend.support.AbstractRepositoryTest;
import com.example.backend.user.entity.Role;
import com.example.backend.user.entity.User;
import com.example.backend.user.repository.UserRepository;

import static org.assertj.core.api.Assertions.assertThat;

class AddressRepositoryTest extends AbstractRepositoryTest {

	@Autowired
	private AddressRepository addressRepository;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private TestEntityManager entityManager;

	@Test
	void findByUserId_pagesOnlyThatUsersAddresses() {
		User first = persistUser("Ana", "ana@mail.com");
		User second = persistUser("Bia", "bia@mail.com");
		persistAddress(first, "Rua A", "1");
		persistAddress(first, "Rua B", "2");
		persistAddress(second, "Rua C", "3");

		Page<Address> page = addressRepository.findByUserId(first.getId(), PageRequest.of(0, 1, Sort.by("street")));

		assertThat(page.getTotalElements()).isEqualTo(2);
		assertThat(page.getContent()).hasSize(1);
		assertThat(page.getContent().get(0).getStreet()).isEqualTo("Rua A");
		assertThat(page.getContent()).allMatch(address -> address.getUser().getId().equals(first.getId()));
	}

	@Test
	void findByIdAndUserId_isEmptyForAnotherUser() {
		User first = persistUser("Ana", "ana@mail.com");
		User second = persistUser("Bia", "bia@mail.com");
		Address address = persistAddress(first, "Rua A", "1");

		assertThat(addressRepository.findByIdAndUserId(address.getId(), second.getId())).isEmpty();
		assertThat(addressRepository.findByIdAndUserId(address.getId(), first.getId())).isPresent();
	}

	@Test
	void deleteUser_removesAddressesInCascade() {
		User user = persistUser("Ana", "ana@mail.com");
		Address address = persistAddress(user, "Rua A", "1");
		Long addressId = address.getId();
		entityManager.clear();

		userRepository.deleteById(user.getId());
		entityManager.flush();
		entityManager.clear();

		assertThat(addressRepository.findById(addressId)).isEmpty();
	}

	private User persistUser(String name, String email) {
		User user = new User();
		user.setName(name);
		user.setEmail(email);
		user.setPassword("encoded");
		user.setRole(Role.USER);
		return userRepository.saveAndFlush(user);
	}

	private Address persistAddress(User user, String street, String number) {
		Address address = new Address();
		address.setStreet(street);
		address.setNumber(number);
		address.setNeighborhood("Centro");
		address.setCity("São Paulo");
		address.setState("SP");
		address.setZipCode("01310100");
		address.setUser(user);
		return addressRepository.saveAndFlush(address);
	}

}
