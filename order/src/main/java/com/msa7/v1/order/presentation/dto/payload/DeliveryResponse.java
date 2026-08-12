package com.msa7.v1.order.presentation.dto.payload;

import java.util.UUID;

public record DeliveryResponse(UUID deliveryId, UUID orderId, String status) {}
