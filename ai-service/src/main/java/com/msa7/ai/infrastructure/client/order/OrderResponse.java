package com.msa7.ai.infrastructure.client.order;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.LocalDateTime;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public record OrderResponse(
        UUID orderId,
        UUID productId,
        int quantity,
        String requestNotes, // 납기일자 및 시간 등 요청사항
        UUID receiverCompanyId
        //LocalDateTime createdAt

) {}