package com.example.backend.address;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import com.example.backend.address.repository.AddressRepository;
import com.example.backend.common.exception.ZipCodeLookupException;
import com.example.backend.support.AbstractIntegrationTest;
import com.example.backend.user.entity.Role;
import com.example.backend.user.entity.User;
import com.example.backend.zipcode.dto.ViaCepResponse;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AddressIntegrationTest extends AbstractIntegrationTest {

	private static final String ZIP_CODE = "01310100";

	@Autowired
	private AddressRepository addressRepository;

	private User user;

	private User other;

	@BeforeEach
	void setUpUsers() {
		user = auth.createUser("User", "user@mail.com", Role.USER);
		other = auth.createUser("Other", "other@mail.com", Role.USER);
		when(viaCepClient.find(ZIP_CODE)).thenReturn(foundViaCep());
	}

	@Test
	void create_asOwner_returnsAddressFromViaCep() throws Exception {
		mockMvc.perform(post("/api/users/{userId}/addresses", user.getId())
						.header(HttpHeaders.AUTHORIZATION, auth.bearer(user))
						.contentType(MediaType.APPLICATION_JSON)
						.content(addressJson("100")))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.street").value("Praça da Sé"))
				.andExpect(jsonPath("$.neighborhood").value("Sé"))
				.andExpect(jsonPath("$.city").value("São Paulo"))
				.andExpect(jsonPath("$.state").value("SP"))
				.andExpect(jsonPath("$.zipCode").value(ZIP_CODE))
				.andExpect(jsonPath("$.number").value("100"))
				.andExpect(jsonPath("$.userId").value(user.getId()));
	}

	@Test
	void create_forAnotherUser_returnsForbidden() throws Exception {
		mockMvc.perform(post("/api/users/{userId}/addresses", user.getId())
						.header(HttpHeaders.AUTHORIZATION, auth.bearer(other))
						.contentType(MediaType.APPLICATION_JSON)
						.content(addressJson("100")))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.message").value("Access denied"));
	}

	@Test
	void list_returnsOnlyThatUsersAddresses() throws Exception {
		createAddress(user, "10");
		createAddress(other, "20");

		mockMvc.perform(get("/api/users/{userId}/addresses", user.getId())
						.header(HttpHeaders.AUTHORIZATION, auth.bearer(user)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content.length()").value(1))
				.andExpect(jsonPath("$.totalElements").value(1))
				.andExpect(jsonPath("$.content[*].number", hasItem("10")))
				.andExpect(jsonPath("$.content[*].number", not(hasItem("20"))))
				.andExpect(jsonPath("$.content[0].userId").value(user.getId()));
	}

	@Test
	void findById_addressOfAnotherUser_returnsNotFound() throws Exception {
		createAddress(other, "20");
		Long addressId = addressRepository.findByUserId(other.getId(), PageRequest.of(0, 10))
				.getContent()
				.get(0)
				.getId();

		mockMvc.perform(get("/api/users/{userId}/addresses/{addressId}", user.getId(), addressId)
						.header(HttpHeaders.AUTHORIZATION, auth.bearer(user)))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.message").value("Address not found"));
	}

	@Test
	void create_invalidZipCode_returnsValidationError() throws Exception {
		mockMvc.perform(post("/api/users/{userId}/addresses", user.getId())
						.header(HttpHeaders.AUTHORIZATION, auth.bearer(user))
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"zipCode":"123","number":"10"}
								"""))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message").value("Validation failed"))
				.andExpect(jsonPath("$.fieldErrors[*].field", hasItem("zipCode")));
	}

	@Test
	void create_unknownZipCode_returnsBadRequest() throws Exception {
		when(viaCepClient.find(ZIP_CODE)).thenReturn(new ViaCepResponse(Boolean.TRUE, "01310-100", "", "", "", "", ""));

		mockMvc.perform(post("/api/users/{userId}/addresses", user.getId())
						.header(HttpHeaders.AUTHORIZATION, auth.bearer(user))
						.contentType(MediaType.APPLICATION_JSON)
						.content(addressJson("100")))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message").value("Invalid zip code"));
	}

	@Test
	void create_viaCepUnavailable_returnsServiceUnavailable() throws Exception {
		when(viaCepClient.find(ZIP_CODE)).thenThrow(new ZipCodeLookupException());

		mockMvc.perform(post("/api/users/{userId}/addresses", user.getId())
						.header(HttpHeaders.AUTHORIZATION, auth.bearer(user))
						.contentType(MediaType.APPLICATION_JSON)
						.content(addressJson("100")))
				.andExpect(status().isServiceUnavailable())
				.andExpect(jsonPath("$.message").value("Zip code service is unavailable"));
	}

	private void createAddress(User owner, String number) throws Exception {
		mockMvc.perform(post("/api/users/{userId}/addresses", owner.getId())
						.header(HttpHeaders.AUTHORIZATION, auth.bearer(owner))
						.contentType(MediaType.APPLICATION_JSON)
						.content(addressJson(number)))
				.andExpect(status().isCreated());
	}

	private String addressJson(String number) {
		return """
				{"zipCode":"%s","number":"%s","complement":"apto 1"}
				""".formatted(ZIP_CODE, number);
	}

	private ViaCepResponse foundViaCep() {
		return new ViaCepResponse(Boolean.FALSE, "01310-100", "Praça da Sé", "", "Sé", "São Paulo", "SP");
	}

}
