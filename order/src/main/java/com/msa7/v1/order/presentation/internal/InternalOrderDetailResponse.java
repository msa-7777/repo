package com.msa7.v1.order.presentation.internal;

import java.util.UUID;

import com.msa7.v1.order.domain.vo.OrderStatus;

public record InternalOrderDetailResponse(
	UUID orderId,
	UUID productId,
	Integer quantity,
	String requestNotes,
	OrderStatus orderStatus,
	UUID receiverCompanyId
) {}
