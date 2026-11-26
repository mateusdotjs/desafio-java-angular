package com.example.backend.common.pagination;

import java.util.Set;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import com.example.backend.common.exception.InvalidSortException;

public final class SortValidator {

	private SortValidator() {
	}

	public static void validate(Pageable pageable, Set<String> allowedFields) {
		for (Sort.Order order : pageable.getSort()) {
			if (!allowedFields.contains(order.getProperty())) {
				throw new InvalidSortException(order.getProperty());
			}
		}
	}

}
