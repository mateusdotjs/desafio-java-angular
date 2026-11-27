package com.example.backend.auth;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import com.example.backend.support.AbstractIntegrationTest;

import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthIntegrationTest extends AbstractIntegrationTest {

	@Test
	void register_validPayload_returnsCreatedToken() throws Exception {
		mockMvc.perform(post("/api/auth/register")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"name":"  Ana  ","email":"Ana@Mail.com","password":"password1"}
								"""))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.tokenType").value("Bearer"))
				.andExpect(jsonPath("$.accessToken").isNotEmpty())
				.andExpect(jsonPath("$.expiresIn").value(86400))
				.andExpect(jsonPath("$.user.name").value("Ana"))
				.andExpect(jsonPath("$.user.email").value("ana@mail.com"))
				.andExpect(jsonPath("$.user.role").value("USER"))
				.andExpect(jsonPath("$.user.password").doesNotExist());
	}

	@Test
	void login_knownUser_returnsToken() throws Exception {
		mockMvc.perform(post("/api/auth/register")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"name":"Ana","email":"ana@mail.com","password":"password1"}
								"""))
				.andExpect(status().isCreated());

		mockMvc.perform(post("/api/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"email":"ANA@mail.com","password":"password1"}
								"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.tokenType").value("Bearer"))
				.andExpect(jsonPath("$.accessToken").isNotEmpty())
				.andExpect(jsonPath("$.user.email").value("ana@mail.com"))
				.andExpect(jsonPath("$.user.password").doesNotExist());
	}

	@Test
	void login_wrongPassword_returnsUnauthorized() throws Exception {
		mockMvc.perform(post("/api/auth/register")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"name":"Ana","email":"ana@mail.com","password":"password1"}
								"""))
				.andExpect(status().isCreated());

		mockMvc.perform(post("/api/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"email":"ana@mail.com","password":"wrongpass"}
								"""))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.message").value("Invalid email or password"))
				.andExpect(jsonPath("$.path").value("/api/auth/login"));
	}

	@Test
	void register_duplicateEmail_returnsConflict() throws Exception {
		mockMvc.perform(post("/api/auth/register")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"name":"Ana","email":"Ana@Mail.com","password":"password1"}
								"""))
				.andExpect(status().isCreated());

		mockMvc.perform(post("/api/auth/register")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"name":"Ana","email":"ana@mail.com","password":"password1"}
								"""))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.message").value("Email already exists"))
				.andExpect(jsonPath("$.path").value("/api/auth/register"));
	}

	@Test
	void register_invalidPayload_returnsValidationError() throws Exception {
		mockMvc.perform(post("/api/auth/register")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"name":"","email":"not-an-email","password":"short"}
								"""))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.status").value(400))
				.andExpect(jsonPath("$.error").value("Bad Request"))
				.andExpect(jsonPath("$.message").value("Validation failed"))
				.andExpect(jsonPath("$.path").value("/api/auth/register"))
				.andExpect(jsonPath("$.timestamp").exists())
				.andExpect(jsonPath("$.fieldErrors[*].field", hasItem("name")))
				.andExpect(jsonPath("$.fieldErrors[*].field", hasItem("email")))
				.andExpect(jsonPath("$.fieldErrors[*].field", hasItem("password")));
	}

	@Test
	void protectedRoute_withoutToken_returnsUnauthorized() throws Exception {
		mockMvc.perform(get("/api/users"))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.status").value(401))
				.andExpect(jsonPath("$.error").value("Unauthorized"))
				.andExpect(jsonPath("$.message").value("Authentication required"))
				.andExpect(jsonPath("$.path").value("/api/users"))
				.andExpect(jsonPath("$.timestamp").exists())
				.andExpect(jsonPath("$.fieldErrors").isEmpty());
	}

}
