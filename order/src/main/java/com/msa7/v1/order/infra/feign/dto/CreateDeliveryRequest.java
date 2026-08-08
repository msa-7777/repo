package com.msa7.v1.order.infra.feign.dto;

import java.util.UUID;

public record CreateDeliveryRequest(UUID orderId, UUID receiverId) {
}
