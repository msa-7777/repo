package com.msa7.ai.infrastructure.client.order;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public record OrderWithDeliveryDto(
        UUID orderId,
        String orderStatus,
        String deliveryStatus,
        UUID deliveryId
) {}