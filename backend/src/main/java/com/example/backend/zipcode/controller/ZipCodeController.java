package com.example.backend.zipcode.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.backend.zipcode.dto.ZipCodeLookupResponse;
import com.example.backend.zipcode.service.ZipCodeService;

@RestController
@RequestMapping("/api/zipcodes")
public class ZipCodeController {

	private final ZipCodeService zipCodeService;

	public ZipCodeController(ZipCodeService zipCodeService) {
		this.zipCodeService = zipCodeService;
	}

	@GetMapping("/{zipCode}")
	public ZipCodeLookupResponse lookup(@PathVariable String zipCode) {
		return zipCodeService.lookup(zipCode);
	}

}