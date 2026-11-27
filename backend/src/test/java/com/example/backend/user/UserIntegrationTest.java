package com.example.backend.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import com.example.backend.support.AbstractIntegrationTest;
import com.example.backend.user.entity.Role;
import com.example.backend.user.entity.User;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class UserIntegrationTest extends AbstractIntegrationTest {

	private User admin;

	private User user;

	private User other;

	@BeforeEach
	void setUpUsers() {
		admin = auth.createUser("Admin", "admin@mail.com", Role.ADMIN);
		user = auth.createUser("User", "user@mail.com", Role.USER);
		other = auth.createUser("Other", "other@mail.com", Role.USER);
	}

	@Test
	void create_asUser_returnsForbidden() throws Exception {
		mockMvc.perform(post("/api/users")
						.header(HttpHeaders.AUTHORIZATION, auth.bearer(user))
						.contentType(MediaType.APPLICATION_JSON)
						.content(userJson("Novo", "novo@mail.com")))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.message").value("Access denied"));
	}

	@Test
	void list_asUser_returnsForbidden() throws Exception {
		mockMvc.perform(get("/api/users")
						.header(HttpHeaders.AUTHORIZATION, auth.bearer(user)))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.message").value("Access denied"));
	}

	@Test
	void create_asAdmin_returnsCreatedUserWithoutPassword() throws Exception {
		mockMvc.perform(post("/api/users")
						.header(HttpHeaders.AUTHORIZATION, auth.bearer(admin))
						.contentType(MediaType.APPLICATION_JSON)
						.content(userJson("  Novo  ", "Novo@Mail.com")))
				.andExpect(status().isCreated())
				.andExpect(header().string(HttpHeaders.LOCATION, containsString("/api/users/")))
				.andExpect(jsonPath("$.name").value("Novo"))
				.andExpect(jsonPath("$.email").value("novo@mail.com"))
				.andExpect(jsonPath("$.role").value("USER"))
				.andExpect(jsonPath("$.password").doesNotExist());
	}

	@Test
	void list_sortByName_returnsPage() throws Exception {
		mockMvc.perform(get("/api/users")
						.header(HttpHeaders.AUTHORIZATION, auth.bearer(admin))
						.param("page", "0")
						.param("size", "20")
						.param("sort", "name,asc"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.number").value(0))
				.andExpect(jsonPath("$.size").value(20))
				.andExpect(jsonPath("$.totalElements").value(3))
				.andExpect(jsonPath("$.content[0].name").value("Admin"))
				.andExpect(jsonPath("$.content[0].password").doesNotExist());
	}

	@Test
	void list_sortByEmail_returnsPage() throws Exception {
		mockMvc.perform(get("/api/users")
						.header(HttpHeaders.AUTHORIZATION, auth.bearer(admin))
						.param("sort", "email,asc"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content[0].email").value("admin@mail.com"))
				.andExpect(jsonPath("$.totalElements").value(3));
	}

	@Test
	void list_disallowedSort_returnsBadRequest() throws Exception {
		mockMvc.perform(get("/api/users")
						.header(HttpHeaders.AUTHORIZATION, auth.bearer(admin))
						.param("sort", "password,asc"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message").value("Sort field not allowed: password"));
	}

	@Test
	void findById_asOwner_returnsOwnUserWithoutPassword() throws Exception {
		mockMvc.perform(get("/api/users/{id}", user.getId())
						.header(HttpHeaders.AUTHORIZATION, auth.bearer(user)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(user.getId()))
				.andExpect(jsonPath("$.email").value("user@mail.com"))
				.andExpect(jsonPath("$.password").doesNotExist());
	}

	@Test
	void findById_asOtherUser_returnsForbidden() throws Exception {
		mockMvc.perform(get("/api/users/{id}", other.getId())
						.header(HttpHeaders.AUTHORIZATION, auth.bearer(user)))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.message").value("Access denied"));
	}

	@Test
	void update_asOwner_changesOwnUser() throws Exception {
		mockMvc.perform(put("/api/users/{id}", user.getId())
						.header(HttpHeaders.AUTHORIZATION, auth.bearer(user))
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"name":"User Edited","email":"user@mail.com"}
								"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.name").value("User Edited"))
				.andExpect(jsonPath("$.password").doesNotExist());
	}

	@Test
	void update_asOtherUser_returnsForbidden() throws Exception {
		mockMvc.perform(put("/api/users/{id}", other.getId())
						.header(HttpHeaders.AUTHORIZATION, auth.bearer(user))
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"name":"Other Edited","email":"other@mail.com"}
								"""))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.message").value("Access denied"));
	}

	@Test
	void delete_asOwner_returnsNoContent() throws Exception {
		mockMvc.perform(delete("/api/users/{id}", user.getId())
						.header(HttpHeaders.AUTHORIZATION, auth.bearer(user)))
				.andExpect(status().isNoContent());
	}

	@Test
	void delete_asOtherUser_returnsForbidden() throws Exception {
		mockMvc.perform(delete("/api/users/{id}", other.getId())
						.header(HttpHeaders.AUTHORIZATION, auth.bearer(user)))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.message").value("Access denied"));
	}

	private String userJson(String name, String email) {
		return """
				{"name":"%s","email":"%s","password":"password1"}
				""".formatted(name, email);
	}

}
