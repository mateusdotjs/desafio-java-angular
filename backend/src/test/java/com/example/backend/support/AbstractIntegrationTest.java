package com.example.backend.support;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.example.backend.security.JwtService;
import com.example.backend.user.repository.UserRepository;
import com.example.backend.zipcode.client.ViaCepClient;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public abstract class AbstractIntegrationTest {

	@Autowired
	protected MockMvc mockMvc;

	@MockitoBean
	protected ViaCepClient viaCepClient;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Autowired
	private JwtService jwtService;

	protected AuthFixture auth;

	@DynamicPropertySource
	static void datasource(DynamicPropertyRegistry registry) {
		PostgresContainer.registerDatasource(registry);
	}

	@BeforeEach
	void setUpAuthFixture() {
		auth = new AuthFixture(userRepository, passwordEncoder, jwtService);
	}

}
