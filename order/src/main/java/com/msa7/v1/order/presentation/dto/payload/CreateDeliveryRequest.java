package com.msa7.v1.order.presentation.dto.payload;

import java.util.UUID;

public record CreateDeliveryRequest(UUID orderId, UUID receiverId) {
}
