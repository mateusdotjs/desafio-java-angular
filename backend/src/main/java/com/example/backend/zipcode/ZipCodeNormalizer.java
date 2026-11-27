package com.example.backend.zipcode;

import com.example.backend.common.exception.InvalidZipCodeException;

public final class ZipCodeNormalizer {

	private ZipCodeNormalizer() {
	}

	public static String normalize(String zipCode) {
		String digits = zipCode.replaceAll("\\D", "");
		if (!digits.matches("\\d{8}")) {
			throw new InvalidZipCodeException();
		}
		return digits;
	}

}