package com.example.backend.auth.service;

import java.time.Instant;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import com.example.backend.auth.dto.AuthResponse;
import com.example.backend.auth.dto.LoginRequest;
import com.example.backend.common.exception.InvalidCredentialsException;
import com.example.backend.security.AuthPrincipal;
import com.example.backend.security.JwtService;
import com.example.backend.user.dto.UserCreateRequest;
import com.example.backend.user.dto.UserResponse;
import com.example.backend.user.entity.Role;
import com.example.backend.user.service.UserService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

	@Mock
	private AuthenticationManager authenticationManager;

	@Mock
	private JwtService jwtService;

	@Mock
	private UserService userService;

	@InjectMocks
	private AuthService authService;

	@Test
	void login_validCredentials_returnsBearerToken() {
		AuthPrincipal principal = AuthPrincipal.fromClaims(4L, "user@mail.com", Role.USER);
		Authentication authentication = new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
		UserResponse user = userResponse(4L, "user@mail.com");
		when(authenticationManager.authenticate(any())).thenReturn(authentication);
		when(userService.findByIdForAuth(4L)).thenReturn(user);
		when(jwtService.generateToken(principal)).thenReturn("signed-token");
		when(jwtService.getExpirationSeconds()).thenReturn(86400L);

		AuthResponse response = authService.login(new LoginRequest("  User@Mail.COM ", "password1"));

		assertThat(response.accessToken()).isEqualTo("signed-token");
		assertThat(response.tokenType()).isEqualTo("Bearer");
		assertThat(response.expiresIn()).isEqualTo(86400L);
		assertThat(response.user()).isEqualTo(user);
		ArgumentCaptor<UsernamePasswordAuthenticationToken> captor = ArgumentCaptor.forClass(
				UsernamePasswordAuthenticationToken.class);
		verify(authenticationManager).authenticate(captor.capture());
		assertThat(captor.getValue().getPrincipal()).isEqualTo("user@mail.com");
		assertThat(captor.getValue().getCredentials()).isEqualTo("password1");
	}

	@Test
	void login_rejectedCredentials_throwsInvalidCredentials() {
		when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("bad"));

		assertThatThrownBy(() -> authService.login(new LoginRequest("user@mail.com", "password1")))
				.isInstanceOf(InvalidCredentialsException.class);
	}

	@Test
	void register_newUser_returnsTokenForCreatedUser() {
		UserCreateRequest request = new UserCreateRequest("Ana", "ana@mail.com", "password1");
		UserResponse user = userResponse(7L, "ana@mail.com");
		when(userService.create(request)).thenReturn(user);
		when(userService.findByIdForAuth(7L)).thenReturn(user);
		when(jwtService.generateToken(any(AuthPrincipal.class))).thenReturn("signed-token");
		when(jwtService.getExpirationSeconds()).thenReturn(86400L);

		AuthResponse response = authService.register(request);

		assertThat(response.accessToken()).isEqualTo("signed-token");
		assertThat(response.tokenType()).isEqualTo("Bearer");
		assertThat(response.user().id()).isEqualTo(7L);
		ArgumentCaptor<AuthPrincipal> captor = ArgumentCaptor.forClass(AuthPrincipal.class);
		verify(jwtService).generateToken(captor.capture());
		assertThat(captor.getValue().id()).isEqualTo(7L);
		assertThat(captor.getValue().email()).isEqualTo("ana@mail.com");
		assertThat(captor.getValue().role()).isEqualTo(Role.USER);
	}

	private UserResponse userResponse(Long id, String email) {
		return new UserResponse(id, "Ana", email, Role.USER, Instant.parse("2026-01-01T00:00:00Z"));
	}

}
