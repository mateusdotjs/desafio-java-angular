package com.example.backend.address.mapper;

import com.example.backend.address.dto.AddressRequest;
import com.example.backend.address.dto.AddressResponse;
import com.example.backend.address.entity.Address;
import com.example.backend.user.entity.User;
import com.example.backend.zipcode.dto.ZipCodeLookupResponse;

public final class AddressMapper {

	private AddressMapper() {
	}

	public static Address toEntity(AddressRequest request, ZipCodeLookupResponse lookup, User user) {
		Address address = new Address();
		apply(address, request, lookup);
		address.setUser(user);
		return address;
	}

	public static void apply(Address address, AddressRequest request, ZipCodeLookupResponse lookup) {
		address.setStreet(lookup.street());
		address.setNumber(request.number().trim());
		address.setComplement(blankToNull(request.complement()));
		address.setNeighborhood(lookup.neighborhood());
		address.setCity(lookup.city());
		address.setState(lookup.state());
		address.setZipCode(lookup.zipCode());
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