package com.example.backend.zipcode.service;

import java.util.Locale;

import org.springframework.stereotype.Service;

import com.example.backend.common.exception.InvalidZipCodeException;
import com.example.backend.zipcode.ZipCodeNormalizer;
import com.example.backend.zipcode.client.ViaCepClient;
import com.example.backend.zipcode.dto.ViaCepResponse;
import com.example.backend.zipcode.dto.ZipCodeLookupResponse;

@Service
public class ZipCodeService {

	private final ViaCepClient viaCepClient;

	public ZipCodeService(ViaCepClient viaCepClient) {
		this.viaCepClient = viaCepClient;
	}

	public ZipCodeLookupResponse lookup(String zipCode) {
		String normalized = ZipCodeNormalizer.normalize(zipCode);
		ViaCepResponse response = viaCepClient.find(normalized);
		if (response.hasError()) {
			throw new InvalidZipCodeException();
		}
		return toLookupResponse(normalized, response);
	}

	private ZipCodeLookupResponse toLookupResponse(String normalizedZipCode, ViaCepResponse response) {
		return new ZipCodeLookupResponse(
				normalizedZipCode,
				requireText(response.logradouro()),
				requireText(response.bairro()),
				requireText(response.localidade()),
				requireText(response.uf()).toUpperCase(Locale.ROOT));
	}

	private String requireText(String value) {
		if (value == null || value.isBlank()) {
			throw new InvalidZipCodeException();
		}
		return value.trim();
	}

}