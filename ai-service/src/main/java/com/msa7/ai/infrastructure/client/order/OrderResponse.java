package com.msa7.ai.infrastructure.client.order;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public record OrderResponse(
        UUID orderId,
        UUID productId,
        Integer quantity,
        String requestNotes,    // 납기일자 및 시간 등 요청사항
        OrderStatus orderStatus,
        UUID receiverCompanyId
) {}