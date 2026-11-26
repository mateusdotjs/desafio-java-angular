package com.example.backend.address.controller;

import java.net.URI;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.example.backend.address.dto.AddressRequest;
import com.example.backend.address.dto.AddressResponse;
import com.example.backend.address.service.AddressService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/users/{userId}/addresses")
public class AddressController {

	private final AddressService addressService;

	public AddressController(AddressService addressService) {
		this.addressService = addressService;
	}

	@PostMapping
	public ResponseEntity<AddressResponse> create(@PathVariable Long userId, @Valid @RequestBody AddressRequest request) {
		AddressResponse response = addressService.create(userId, request);
		URI location = ServletUriComponentsBuilder.fromCurrentRequest()
				.path("/{addressId}")
				.buildAndExpand(response.id())
				.toUri();
		return ResponseEntity.created(location).body(response);
	}

	@GetMapping
	public Page<AddressResponse> list(
			@PathVariable Long userId,
			@PageableDefault(size = 20, sort = "street") Pageable pageable) {
		return addressService.list(userId, pageable);
	}

	@GetMapping("/{addressId}")
	public AddressResponse findById(@PathVariable Long userId, @PathVariable Long addressId) {
		return addressService.findById(userId, addressId);
	}

	@PutMapping("/{addressId}")
	public AddressResponse update(
			@PathVariable Long userId,
			@PathVariable Long addressId,
			@Valid @RequestBody AddressRequest request) {
		return addressService.update(userId, addressId, request);
	}

	@DeleteMapping("/{addressId}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void delete(@PathVariable Long userId, @PathVariable Long addressId) {
		addressService.delete(userId, addressId);
	}

}
