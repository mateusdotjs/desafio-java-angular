package com.example.backend.zipcode.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import com.example.backend.common.exception.InvalidZipCodeException;
import com.example.backend.common.exception.ZipCodeLookupException;
import com.example.backend.zipcode.dto.ViaCepResponse;

@Component
public class ViaCepClient {

	private static final Logger LOGGER = LoggerFactory.getLogger(ViaCepClient.class);

	private final RestClient restClient;

	public ViaCepClient(RestClient viaCepRestClient) {
		this.restClient = viaCepRestClient;
	}

	public ViaCepResponse find(String zipCode) {
		try {
			ViaCepResponse response = restClient.get()
					.uri("/ws/{zipCode}/json/", zipCode)
					.retrieve()
					.onStatus(HttpStatusCode::is4xxClientError, (request, clientResponse) -> {
						throw new InvalidZipCodeException();
					})
					.onStatus(HttpStatusCode::is5xxServerError, (request, clientResponse) -> {
						throw new ZipCodeLookupException();
					})
					.body(ViaCepResponse.class);

			if (response == null) {
				throw new ZipCodeLookupException();
			}
			return response;
		}
		catch (InvalidZipCodeException | ZipCodeLookupException exception) {
			throw exception;
		}
		catch (RestClientException exception) {
			if (exception.getCause() instanceof InvalidZipCodeException invalidZipCode) {
				throw invalidZipCode;
			}
			if (exception.getCause() instanceof ZipCodeLookupException zipCodeLookup) {
				throw zipCodeLookup;
			}
			LOGGER.warn("ViaCEP request failed", exception);
			throw new ZipCodeLookupException();
		}
	}

}
