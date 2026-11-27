package com.example.backend.zipcode.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ViaCepResponse(
		@JsonProperty("erro") Object erro,
		String cep,
		String logradouro,
		String complemento,
		String bairro,
		String localidade,
		String uf) {

	public boolean hasError() {
		if (erro == null) {
			return false;
		}
		if (erro instanceof Boolean flag) {
			return flag;
		}
		if (erro instanceof String value) {
			return "true".equalsIgnoreCase(value);
		}
		return false;
	}

}