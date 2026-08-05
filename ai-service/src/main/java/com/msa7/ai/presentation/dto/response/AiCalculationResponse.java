package com.msa7.ai.presentation.dto.response;

import com.msa7.ai.domain.model.AiHistory;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class AiCalculationResponse {

    private UUID historyId;
    private UUID orderId;
    private String promptRequest;
    private LocalDateTime calculatedDeadline;
    private String generatedMessage;
    private Boolean isSlackNotified;
    private LocalDateTime createdAt;

    public static AiCalculationResponse from(AiHistory entity) {
        return AiCalculationResponse.builder()
                .historyId(entity.getHistoryId())
                .orderId(entity.getOrderId())
                .promptRequest(entity.getPromptRequest())
                .calculatedDeadline(entity.getCalculatedDeadline())
                .generatedMessage(entity.getGeneratedMessage())
                .isSlackNotified(entity.getIsSlackNotified())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}