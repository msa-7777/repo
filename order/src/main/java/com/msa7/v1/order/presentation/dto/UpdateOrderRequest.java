package com.msa7.v1.order.presentation.dto;

public record UpdateOrderRequest(
	Integer quantity,
	String requestNotes
) {
}
