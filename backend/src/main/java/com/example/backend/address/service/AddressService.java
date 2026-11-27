package com.example.backend.address.service;

import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import com.example.backend.address.dto.AddressRequest;
import com.example.backend.address.dto.AddressResponse;
import com.example.backend.address.entity.Address;
import com.example.backend.address.mapper.AddressMapper;
import com.example.backend.address.repository.AddressRepository;
import com.example.backend.common.exception.ResourceNotFoundException;
import com.example.backend.common.pagination.SortValidator;
import com.example.backend.security.SecurityUtils;
import com.example.backend.user.entity.User;
import com.example.backend.user.repository.UserRepository;
import com.example.backend.zipcode.dto.ZipCodeLookupResponse;
import com.example.backend.zipcode.service.ZipCodeService;

@Service
@Transactional(readOnly = true)
public class AddressService {

	private static final Set<String> ALLOWED_SORTS = Set.of("street", "neighborhood", "city", "state", "zipCode");

	private final AddressRepository addressRepository;

	private final UserRepository userRepository;

	private final ZipCodeService zipCodeService;

	private final TransactionTemplate transactionTemplate;

	public AddressService(
			AddressRepository addressRepository,
			UserRepository userRepository,
			ZipCodeService zipCodeService,
			PlatformTransactionManager transactionManager) {
		this.addressRepository = addressRepository;
		this.userRepository = userRepository;
		this.zipCodeService = zipCodeService;
		this.transactionTemplate = new TransactionTemplate(transactionManager);
	}

	public AddressResponse create(Long userId, AddressRequest request) {
		SecurityUtils.assertAdminOrOwner(userId);
		ZipCodeLookupResponse lookup = zipCodeService.lookup(request.zipCode());
		return transactionTemplate.execute(status -> {
			User user = getUser(userId);
			Address address = AddressMapper.toEntity(request, lookup, user);
			return AddressMapper.toResponse(addressRepository.save(address));
		});
	}

	public Page<AddressResponse> list(Long userId, Pageable pageable) {
		SecurityUtils.assertAdminOrOwner(userId);
		SortValidator.validate(pageable, ALLOWED_SORTS);
		getUser(userId);
		return addressRepository.findByUserId(userId, pageable).map(AddressMapper::toResponse);
	}

	public AddressResponse findById(Long userId, Long addressId) {
		SecurityUtils.assertAdminOrOwner(userId);
		return AddressMapper.toResponse(getAddress(userId, addressId));
	}

	public AddressResponse update(Long userId, Long addressId, AddressRequest request) {
		SecurityUtils.assertAdminOrOwner(userId);
		ZipCodeLookupResponse lookup = zipCodeService.lookup(request.zipCode());
		return transactionTemplate.execute(status -> {
			Address address = getAddress(userId, addressId);
			AddressMapper.apply(address, request, lookup);
			return AddressMapper.toResponse(address);
		});
	}

	@Transactional
	public void delete(Long userId, Long addressId) {
		SecurityUtils.assertAdminOrOwner(userId);
		Address address = getAddress(userId, addressId);
		addressRepository.delete(address);
	}

	private User getUser(Long userId) {
		return userRepository.findById(userId)
				.orElseThrow(() -> new ResourceNotFoundException("User not found"));
	}

	private Address getAddress(Long userId, Long addressId) {
		return addressRepository.findByIdAndUserId(addressId, userId)
				.orElseThrow(() -> new ResourceNotFoundException("Address not found"));
	}

}