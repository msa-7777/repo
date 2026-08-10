package com.msa7.v1.delivery.domain.vo;

public record DestinationAddr(String address) {
	public DestinationAddr {
		if (address == null || address.trim().isEmpty()) {
			throw new IllegalArgumentException("Address cannot be null or empty");
		}
	}
}
