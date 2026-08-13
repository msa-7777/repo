package com.msa7.v1.delivery.presentation.dto.payload;

import java.util.UUID;

public record DeliveryCompletedEvent(
	UUID orderId,
	UUID deliveryId,
	String message
) {}
