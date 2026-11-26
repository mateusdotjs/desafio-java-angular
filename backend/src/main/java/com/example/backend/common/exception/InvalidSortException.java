package com.example.backend.common.exception;

public class InvalidSortException extends RuntimeException {

	public InvalidSortException(String field) {
		super("Sort field not allowed: " + field);
	}

}
