package com.msa7.v1.delivery.presentation.dto.payload;

import java.util.UUID;

import com.msa7.v1.delivery.domain.vo.DeliveryStatus;

public record DeliveryResponse(
	UUID deliveryId,
	UUID orderId,
	DeliveryStatus status
) {
}
