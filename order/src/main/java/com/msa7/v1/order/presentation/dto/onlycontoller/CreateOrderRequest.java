package com.msa7.v1.order.presentation.dto.onlycontoller;

import java.util.UUID;

public record CreateOrderRequest(
	UUID receiverCompanyId,
	UUID productId,
	Integer quantity,
	String requestNotes
) {}
