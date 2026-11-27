package com.example.backend.support;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.testcontainers.postgresql.PostgreSQLContainer;

public final class PostgresContainer {

	private static final PostgreSQLContainer POSTGRES = new PostgreSQLContainer("postgres:16-alpine");

	static {
		POSTGRES.start();
	}

	private PostgresContainer() {
	}

	public static void registerDatasource(DynamicPropertyRegistry registry) {
		registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
		registry.add("spring.datasource.username", POSTGRES::getUsername);
		registry.add("spring.datasource.password", POSTGRES::getPassword);
	}

}
