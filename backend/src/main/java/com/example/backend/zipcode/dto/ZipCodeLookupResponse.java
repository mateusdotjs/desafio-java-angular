package com.example.backend.zipcode.dto;

public record ZipCodeLookupResponse(
		String zipCode,
		String street,
		String neighborhood,
		String city,
		String state) {
}