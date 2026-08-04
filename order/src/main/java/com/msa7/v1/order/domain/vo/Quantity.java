package com.msa7.v1.order.domain.vo;

public record Quantity(Integer value) {
	public Quantity {
		if (value <= 0){
			throw new IllegalArgumentException("수량은 0보다 커야함");
		}
	}
}
