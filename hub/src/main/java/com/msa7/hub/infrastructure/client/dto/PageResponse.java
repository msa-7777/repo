package com.msa7.hub.infrastructure.client.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record PageResponse<T>(
	List<T> content,
	long totalElements
) {
}
