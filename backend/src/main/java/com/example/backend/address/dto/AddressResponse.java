package com.example.backend.address.dto;

public record AddressResponse(
		Long id,
		String street,
		String number,
		String complement,
		String neighborhood,
		String city,
		String state,
		String zipCode,
		Long userId) {
}
