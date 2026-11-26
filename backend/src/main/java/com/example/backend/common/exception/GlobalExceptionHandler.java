package com.example.backend.common.exception;

import java.time.Instant;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import com.example.backend.common.response.ErrorResponse;
import com.example.backend.common.response.FieldErrorResponse;

import jakarta.servlet.http.HttpServletRequest;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(ResourceNotFoundException.class)
	public ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException exception, HttpServletRequest request) {
		return error(HttpStatus.NOT_FOUND, exception.getMessage(), request, List.of());
	}

	@ExceptionHandler(EmailAlreadyExistsException.class)
	public ResponseEntity<ErrorResponse> handleConflict(EmailAlreadyExistsException exception, HttpServletRequest request) {
		return error(HttpStatus.CONFLICT, exception.getMessage(), request, List.of());
	}

	@ExceptionHandler(InvalidZipCodeException.class)
	public ResponseEntity<ErrorResponse> handleInvalidZipCode(InvalidZipCodeException exception, HttpServletRequest request) {
		return error(HttpStatus.BAD_REQUEST, exception.getMessage(), request, List.of());
	}

	@ExceptionHandler(InvalidSortException.class)
	public ResponseEntity<ErrorResponse> handleInvalidSort(InvalidSortException exception, HttpServletRequest request) {
		return error(HttpStatus.BAD_REQUEST, exception.getMessage(), request, List.of());
	}

	@ExceptionHandler(ZipCodeLookupException.class)
	public ResponseEntity<ErrorResponse> handleZipCodeLookup(ZipCodeLookupException exception, HttpServletRequest request) {
		return error(HttpStatus.SERVICE_UNAVAILABLE, exception.getMessage(), request, List.of());
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException exception, HttpServletRequest request) {
		List<FieldErrorResponse> fieldErrors = exception.getBindingResult().getFieldErrors().stream()
				.map(fieldError -> new FieldErrorResponse(
						fieldError.getField(),
						fieldError.getDefaultMessage() == null ? "Invalid value" : fieldError.getDefaultMessage()))
				.toList();
		return error(HttpStatus.BAD_REQUEST, "Validation failed", request, fieldErrors);
	}

	@ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class})
	public ResponseEntity<ErrorResponse> handleBadRequest(Exception exception, HttpServletRequest request) {
		return error(HttpStatus.BAD_REQUEST, "Malformed request", request, List.of());
	}

	private ResponseEntity<ErrorResponse> error(
			HttpStatus status,
			String message,
			HttpServletRequest request,
			List<FieldErrorResponse> fieldErrors) {
		ErrorResponse body = new ErrorResponse(
				Instant.now(),
				status.value(),
				status.getReasonPhrase(),
				message,
				request.getRequestURI(),
				fieldErrors);
		return ResponseEntity.status(status).body(body);
	}

}
