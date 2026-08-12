package com.msa7.v1.order.presentation.dto.payload;

import java.util.UUID;

import com.msa7.v1.order.domain.aggregate.Order;

public record OrderWithDeliveryDto(UUID orderId,
								   String orderStatus,
								   String deliveryStatus,
								   UUID deliveryId)
{
	public OrderWithDeliveryDto(Order order, String deliveryStatus) {
		this(order.getId(), order.getStatus().name(),
			deliveryStatus, order.getDeliveryId());
	}
}