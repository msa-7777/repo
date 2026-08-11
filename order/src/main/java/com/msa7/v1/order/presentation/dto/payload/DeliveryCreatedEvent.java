package com.msa7.v1.order.presentation.dto.payload;

import java.util.UUID;

public record DeliveryCreatedEvent(
	UUID orderId,
	UUID deliveryId
) {
}
