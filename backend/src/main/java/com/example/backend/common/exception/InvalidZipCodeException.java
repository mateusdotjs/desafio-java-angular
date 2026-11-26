package com.example.backend.common.exception;

public class InvalidZipCodeException extends RuntimeException {

	public InvalidZipCodeException() {
		super("Invalid zip code");
	}

}
