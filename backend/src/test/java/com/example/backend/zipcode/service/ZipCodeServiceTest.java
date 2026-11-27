package com.example.backend.zipcode.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.backend.common.exception.InvalidZipCodeException;
import com.example.backend.common.exception.ZipCodeLookupException;
import com.example.backend.zipcode.client.ViaCepClient;
import com.example.backend.zipcode.dto.ViaCepResponse;
import com.example.backend.zipcode.dto.ZipCodeLookupResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ZipCodeServiceTest {

	@Mock
	private ViaCepClient viaCepClient;

	@InjectMocks
	private ZipCodeService zipCodeService;

	@Test
	void lookup_maskedZipCode_normalizesAndUppercasesState() {
		when(viaCepClient.find("01310100")).thenReturn(viaCep(Boolean.FALSE, "  Praça da Sé  ", "Sé", "São Paulo", " sp "));

		ZipCodeLookupResponse response = zipCodeService.lookup("01310-100");

		assertThat(response.zipCode()).isEqualTo("01310100");
		assertThat(response.street()).isEqualTo("Praça da Sé");
		assertThat(response.neighborhood()).isEqualTo("Sé");
		assertThat(response.city()).isEqualTo("São Paulo");
		assertThat(response.state()).isEqualTo("SP");
		verify(viaCepClient).find("01310100");
	}

	@Test
	void lookup_errorFlag_throwsInvalidZipCode() {
		when(viaCepClient.find("01310100")).thenReturn(viaCep(Boolean.TRUE, "Praça da Sé", "Sé", "São Paulo", "SP"));

		assertThatThrownBy(() -> zipCodeService.lookup("01310100"))
				.isInstanceOf(InvalidZipCodeException.class);
	}

	@Test
	void lookup_blankRequiredField_throwsInvalidZipCode() {
		when(viaCepClient.find("01310100")).thenReturn(viaCep(Boolean.FALSE, "   ", "Sé", "São Paulo", "SP"));

		assertThatThrownBy(() -> zipCodeService.lookup("01310100"))
				.isInstanceOf(InvalidZipCodeException.class);
	}

	@Test
	void lookup_invalidZipCode_throwsBeforeCallingClient() {
		assertThatThrownBy(() -> zipCodeService.lookup("123"))
				.isInstanceOf(InvalidZipCodeException.class);
		verifyNoInteractions(viaCepClient);
	}

	@Test
	void lookup_clientUnavailable_propagatesZipCodeLookupException() {
		when(viaCepClient.find("01310100")).thenThrow(new ZipCodeLookupException());

		assertThatThrownBy(() -> zipCodeService.lookup("01310100"))
				.isInstanceOf(ZipCodeLookupException.class);
	}

	private ViaCepResponse viaCep(Object error, String street, String neighborhood, String city, String state) {
		return new ViaCepResponse(error, "01310-100", street, "", neighborhood, city, state);
	}

}
