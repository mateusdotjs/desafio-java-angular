package com.example.backend.security;

import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.example.backend.user.entity.Role;
import com.example.backend.user.entity.User;

public record AuthPrincipal(Long id, String email, String passwordHash, Role role) implements UserDetails {

	public static AuthPrincipal from(User user) {
		return new AuthPrincipal(user.getId(), user.getEmail(), user.getPassword(), user.getRole());
	}

	public static AuthPrincipal fromClaims(Long id, String email, Role role) {
		return new AuthPrincipal(id, email, "", role);
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
	}

	@Override
	public String getPassword() {
		return passwordHash;
	}

	@Override
	public String getUsername() {
		return email;
	}

	@Override
	public boolean isAccountNonExpired() {
		return true;
	}

	@Override
	public boolean isAccountNonLocked() {
		return true;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}

	@Override
	public boolean isEnabled() {
		return true;
	}

}