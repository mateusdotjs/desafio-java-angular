package com.example.backend.address.mapper;

import java.util.Locale;

import com.example.backend.address.dto.AddressRequest;
import com.example.backend.address.dto.AddressResponse;
import com.example.backend.address.entity.Address;
import com.example.backend.user.entity.User;

public final class AddressMapper {

	private AddressMapper() {
	}

	public static Address toEntity(AddressRequest request, String zipCode, User user) {
		Address address = new Address();
		apply(address, request, zipCode);
		address.setUser(user);
		return address;
	}

	public static void apply(Address address, AddressRequest request, String zipCode) {
		address.setStreet(request.street().trim());
		address.setNumber(request.number().trim());
		address.setComplement(blankToNull(request.complement()));
		address.setNeighborhood(request.neighborhood().trim());
		address.setCity(request.city().trim());
		address.setState(request.state().trim().toUpperCase(Locale.ROOT));
		address.setZipCode(zipCode);
	}

	public static AddressResponse toResponse(Address address) {
		return new AddressResponse(
				address.getId(),
				address.getStreet(),
				address.getNumber(),
				address.getComplement(),
				address.getNeighborhood(),
				address.getCity(),
				address.getState(),
				address.getZipCode(),
				address.getUser().getId());
	}

	private static String blankToNull(String value) {
		if (value == null || value.isBlank()) {
			return null;
		}
		return value.trim();
	}

}
