package com.example.backend.common.exception;

public class ForbiddenAccessException extends RuntimeException {

	public ForbiddenAccessException() {
		super("Access denied");
	}

}