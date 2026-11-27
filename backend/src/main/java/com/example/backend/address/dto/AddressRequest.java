package com.example.backend.address.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record AddressRequest(
		@NotBlank @Pattern(regexp = "^\\d{8}$") String zipCode,
		@NotBlank @Size(max = 20) String number,
		@Size(max = 255) String complement) {
}