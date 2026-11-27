package com.example.backend.address.service;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;

import com.example.backend.address.dto.AddressRequest;
import com.example.backend.address.dto.AddressResponse;
import com.example.backend.address.entity.Address;
import com.example.backend.address.repository.AddressRepository;
import com.example.backend.common.exception.ForbiddenAccessException;
import com.example.backend.common.exception.InvalidSortException;
import com.example.backend.common.exception.ResourceNotFoundException;
import com.example.backend.support.SecurityTestSupport;
import com.example.backend.user.entity.Role;
import com.example.backend.user.entity.User;
import com.example.backend.user.repository.UserRepository;
import com.example.backend.zipcode.dto.ZipCodeLookupResponse;
import com.example.backend.zipcode.service.ZipCodeService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AddressServiceTest {

	private static final ZipCodeLookupResponse LOOKUP = new ZipCodeLookupResponse(
			"01310100",
			"Praça da Sé",
			"Sé",
			"São Paulo",
			"SP");

	@Mock
	private AddressRepository addressRepository;

	@Mock
	private UserRepository userRepository;

	@Mock
	private ZipCodeService zipCodeService;

	@Mock
	private PlatformTransactionManager transactionManager;

	@InjectMocks
	private AddressService addressService;

	@BeforeEach
	void setUpTransaction() {
		lenient().when(transactionManager.getTransaction(any())).thenReturn(mock(TransactionStatus.class));
	}

	@AfterEach
	void clearSecurity() {
		SecurityTestSupport.clear();
	}

	@Test
	void create_asOwner_storesLookupData() {
		User user = user(10L);
		when(zipCodeService.lookup("01310100")).thenReturn(LOOKUP);
		when(userRepository.findById(10L)).thenReturn(Optional.of(user));
		when(addressRepository.save(any(Address.class))).thenAnswer(invocation -> {
			Address address = invocation.getArgument(0);
			address.setId(5L);
			return address;
		});
		SecurityTestSupport.authenticate(10L, "user@mail.com", Role.USER);

		AddressResponse response = addressService.create(10L, new AddressRequest("01310100", "  100  ", "  apto 1  "));

		assertThat(response.id()).isEqualTo(5L);
		assertThat(response.street()).isEqualTo("Praça da Sé");
		assertThat(response.number()).isEqualTo("100");
		assertThat(response.complement()).isEqualTo("apto 1");
		assertThat(response.neighborhood()).isEqualTo("Sé");
		assertThat(response.city()).isEqualTo("São Paulo");
		assertThat(response.state()).isEqualTo("SP");
		assertThat(response.zipCode()).isEqualTo("01310100");
		assertThat(response.userId()).isEqualTo(10L);
	}

	@Test
	void create_blankComplement_storesNull() {
		User user = user(10L);
		when(zipCodeService.lookup("01310100")).thenReturn(LOOKUP);
		when(userRepository.findById(10L)).thenReturn(Optional.of(user));
		when(addressRepository.save(any(Address.class))).thenAnswer(invocation -> invocation.getArgument(0));
		SecurityTestSupport.authenticate(1L, "admin@mail.com", Role.ADMIN);

		AddressResponse response = addressService.create(10L, new AddressRequest("01310100", "100", "   "));

		assertThat(response.complement()).isNull();
		assertThat(response.street()).isEqualTo("Praça da Sé");
	}

	@Test
	void create_asOtherUser_throwsForbidden() {
		SecurityTestSupport.authenticate(11L, "other@mail.com", Role.USER);

		assertThatThrownBy(() -> addressService.create(10L, addressRequest()))
				.isInstanceOf(ForbiddenAccessException.class);
	}

	@Test
	void create_missingUser_throwsNotFound() {
		when(zipCodeService.lookup("01310100")).thenReturn(LOOKUP);
		when(userRepository.findById(10L)).thenReturn(Optional.empty());
		SecurityTestSupport.authenticate(1L, "admin@mail.com", Role.ADMIN);

		assertThatThrownBy(() -> addressService.create(10L, addressRequest()))
				.isInstanceOf(ResourceNotFoundException.class)
				.hasMessage("User not found");
	}

	@Test
	void list_allowedSorts_returnOnlyRequestedUser() {
		User user = user(10L);
		Address address = address(5L, user);
		when(userRepository.findById(10L)).thenReturn(Optional.of(user));
		SecurityTestSupport.authenticate(10L, "user@mail.com", Role.USER);
		for (String field : List.of("street", "neighborhood", "city", "state", "zipCode")) {
			Pageable pageable = PageRequest.of(0, 20, Sort.by(field));
			when(addressRepository.findByUserId(10L, pageable)).thenReturn(new PageImpl<>(List.of(address), pageable, 1));

			Page<AddressResponse> page = addressService.list(10L, pageable);

			assertThat(page.getContent()).extracting(AddressResponse::userId).containsExactly(10L);
			assertThat(page.getContent().get(0).street()).isEqualTo("Praça da Sé");
		}
	}

	@Test
	void list_asOtherUser_throwsForbidden() {
		SecurityTestSupport.authenticate(11L, "other@mail.com", Role.USER);

		assertThatThrownBy(() -> addressService.list(10L, PageRequest.of(0, 20)))
				.isInstanceOf(ForbiddenAccessException.class);
	}

	@Test
	void list_disallowedSort_throwsInvalidSort() {
		SecurityTestSupport.authenticate(1L, "admin@mail.com", Role.ADMIN);
		Pageable pageable = PageRequest.of(0, 20, Sort.by("createdAt"));

		assertThatThrownBy(() -> addressService.list(10L, pageable))
				.isInstanceOf(InvalidSortException.class);
	}

	@Test
	void list_missingUser_throwsNotFound() {
		when(userRepository.findById(10L)).thenReturn(Optional.empty());
		SecurityTestSupport.authenticate(1L, "admin@mail.com", Role.ADMIN);

		assertThatThrownBy(() -> addressService.list(10L, PageRequest.of(0, 20, Sort.by("street"))))
				.isInstanceOf(ResourceNotFoundException.class)
				.hasMessage("User not found");
	}

	@Test
	void findById_asOwner_returnsAddress() {
		when(addressRepository.findByIdAndUserId(5L, 10L)).thenReturn(Optional.of(address(5L, user(10L))));
		SecurityTestSupport.authenticate(10L, "user@mail.com", Role.USER);

		AddressResponse response = addressService.findById(10L, 5L);

		assertThat(response.id()).isEqualTo(5L);
		assertThat(response.userId()).isEqualTo(10L);
	}

	@Test
	void findById_addressOfAnotherOwner_throwsNotFound() {
		when(addressRepository.findByIdAndUserId(5L, 10L)).thenReturn(Optional.empty());
		SecurityTestSupport.authenticate(10L, "user@mail.com", Role.USER);

		assertThatThrownBy(() -> addressService.findById(10L, 5L))
				.isInstanceOf(ResourceNotFoundException.class)
				.hasMessage("Address not found");
	}

	@Test
	void findById_asOtherUser_throwsForbidden() {
		SecurityTestSupport.authenticate(11L, "other@mail.com", Role.USER);

		assertThatThrownBy(() -> addressService.findById(10L, 5L))
				.isInstanceOf(ForbiddenAccessException.class);
	}

	@Test
	void update_asOwner_replacesFieldsFromLookup() {
		Address address = address(5L, user(10L));
		ZipCodeLookupResponse updated = new ZipCodeLookupResponse("01001000", "Rua Nova", "Centro", "Campinas", "SP");
		when(zipCodeService.lookup("01001000")).thenReturn(updated);
		when(addressRepository.findByIdAndUserId(5L, 10L)).thenReturn(Optional.of(address));
		SecurityTestSupport.authenticate(10L, "user@mail.com", Role.USER);

		AddressResponse response = addressService.update(10L, 5L, new AddressRequest("01001000", "  200  ", null));

		assertThat(response.street()).isEqualTo("Rua Nova");
		assertThat(response.neighborhood()).isEqualTo("Centro");
		assertThat(response.city()).isEqualTo("Campinas");
		assertThat(response.state()).isEqualTo("SP");
		assertThat(response.zipCode()).isEqualTo("01001000");
		assertThat(response.number()).isEqualTo("200");
		assertThat(response.complement()).isNull();
	}

	@Test
	void update_asAdmin_updatesAnotherUsersAddress() {
		Address address = address(5L, user(10L));
		when(zipCodeService.lookup("01310100")).thenReturn(LOOKUP);
		when(addressRepository.findByIdAndUserId(5L, 10L)).thenReturn(Optional.of(address));
		SecurityTestSupport.authenticate(1L, "admin@mail.com", Role.ADMIN);

		AddressResponse response = addressService.update(10L, 5L, addressRequest());

		assertThat(response.userId()).isEqualTo(10L);
		assertThat(response.street()).isEqualTo("Praça da Sé");
	}

	@Test
	void update_missingAddress_throwsNotFound() {
		when(zipCodeService.lookup("01310100")).thenReturn(LOOKUP);
		when(addressRepository.findByIdAndUserId(5L, 10L)).thenReturn(Optional.empty());
		SecurityTestSupport.authenticate(10L, "user@mail.com", Role.USER);

		assertThatThrownBy(() -> addressService.update(10L, 5L, addressRequest()))
				.isInstanceOf(ResourceNotFoundException.class)
				.hasMessage("Address not found");
	}

	@Test
	void update_asOtherUser_throwsForbidden() {
		SecurityTestSupport.authenticate(11L, "other@mail.com", Role.USER);

		assertThatThrownBy(() -> addressService.update(10L, 5L, addressRequest()))
				.isInstanceOf(ForbiddenAccessException.class);
	}

	@Test
	void delete_asOwner_removesAddress() {
		Address address = address(5L, user(10L));
		when(addressRepository.findByIdAndUserId(5L, 10L)).thenReturn(Optional.of(address));
		SecurityTestSupport.authenticate(10L, "user@mail.com", Role.USER);

		addressService.delete(10L, 5L);

		verify(addressRepository).delete(address);
	}

	@Test
	void delete_asAdmin_removesAnotherUsersAddress() {
		Address address = address(5L, user(10L));
		when(addressRepository.findByIdAndUserId(5L, 10L)).thenReturn(Optional.of(address));
		SecurityTestSupport.authenticate(1L, "admin@mail.com", Role.ADMIN);

		addressService.delete(10L, 5L);

		verify(addressRepository).delete(address);
	}

	@Test
	void delete_missingAddress_throwsNotFound() {
		when(addressRepository.findByIdAndUserId(5L, 10L)).thenReturn(Optional.empty());
		SecurityTestSupport.authenticate(10L, "user@mail.com", Role.USER);

		assertThatThrownBy(() -> addressService.delete(10L, 5L))
				.isInstanceOf(ResourceNotFoundException.class)
				.hasMessage("Address not found");
	}

	@Test
	void delete_asOtherUser_throwsForbidden() {
		SecurityTestSupport.authenticate(11L, "other@mail.com", Role.USER);

		assertThatThrownBy(() -> addressService.delete(10L, 5L))
				.isInstanceOf(ForbiddenAccessException.class);
	}

	private AddressRequest addressRequest() {
		return new AddressRequest("01310100", "100", "apto 1");
	}

	private User user(Long id) {
		User user = new User();
		user.setId(id);
		user.setName("Ana");
		user.setEmail("user@mail.com");
		user.setRole(Role.USER);
		return user;
	}

	private Address address(Long id, User user) {
		Address address = new Address();
		address.setId(id);
		address.setStreet("Praça da Sé");
		address.setNumber("100");
		address.setNeighborhood("Sé");
		address.setCity("São Paulo");
		address.setState("SP");
		address.setZipCode("01310100");
		address.setUser(user);
		return address;
	}

}
