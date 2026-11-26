package com.example.backend.zipcode.service;

import org.springframework.stereotype.Service;

import com.example.backend.common.exception.InvalidZipCodeException;
import com.example.backend.zipcode.client.ViaCepClient;
import com.example.backend.zipcode.dto.ViaCepResponse;

@Service
public class ZipCodeService {

	private final ViaCepClient viaCepClient;

	public ZipCodeService(ViaCepClient viaCepClient) {
		this.viaCepClient = viaCepClient;
	}

	public void validate(String zipCode) {
		ViaCepResponse response = viaCepClient.find(zipCode);
		if (Boolean.TRUE.equals(response.erro())) {
			throw new InvalidZipCodeException();
		}
	}

}
