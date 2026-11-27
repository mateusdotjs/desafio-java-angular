package com.example.backend.zipcode;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;

import com.example.backend.support.AbstractIntegrationTest;
import com.example.backend.user.entity.Role;
import com.example.backend.user.entity.User;
import com.example.backend.zipcode.dto.ViaCepResponse;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ZipCodeIntegrationTest extends AbstractIntegrationTest {

	private User user;

	@BeforeEach
	void setUpUser() {
		user = auth.createUser("User", "user@mail.com", Role.USER);
		when(viaCepClient.find("01310100")).thenReturn(
				new ViaCepResponse(Boolean.FALSE, "01310-100", "Praça da Sé", "", "Sé", "São Paulo", "SP"));
	}

	@Test
	void lookup_authenticated_returnsAddress() throws Exception {
		mockMvc.perform(get("/api/zipcodes/{zipCode}", "01310100")
						.header(HttpHeaders.AUTHORIZATION, auth.bearer(user)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.zipCode").value("01310100"))
				.andExpect(jsonPath("$.street").value("Praça da Sé"))
				.andExpect(jsonPath("$.neighborhood").value("Sé"))
				.andExpect(jsonPath("$.city").value("São Paulo"))
				.andExpect(jsonPath("$.state").value("SP"));
	}

	@Test
	void lookup_withoutToken_returnsUnauthorized() throws Exception {
		mockMvc.perform(get("/api/zipcodes/{zipCode}", "01310100"))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.message").value("Authentication required"));
	}

	@Test
	void lookup_invalidZipCode_returnsBadRequest() throws Exception {
		mockMvc.perform(get("/api/zipcodes/{zipCode}", "123")
						.header(HttpHeaders.AUTHORIZATION, auth.bearer(user)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message").value("Invalid zip code"));
	}

}
