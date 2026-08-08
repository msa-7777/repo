package com.msa7.v1.order.presentation.internal;

import java.util.UUID;

import com.msa7.v1.order.domain.vo.OrderStatus;

import lombok.Builder;

@Builder
public record InternalOrderResponse(
	UUID orderId,
	UUID receiverCompanyId,
	OrderStatus status
) {
}
