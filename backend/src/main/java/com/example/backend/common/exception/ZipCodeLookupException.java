package com.example.backend.common.exception;

public class ZipCodeLookupException extends RuntimeException {

	public ZipCodeLookupException() {
		super("Zip code service is unavailable");
	}

}
