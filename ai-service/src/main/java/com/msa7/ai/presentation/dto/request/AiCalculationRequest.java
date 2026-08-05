package com.msa7.ai.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AiCalculationRequest {

    @NotNull(message = "주문 ID(orderId)는 필수입니다.")
    private UUID orderId;

    @NotBlank(message = "프롬프트 요청 내용(promptRequest)은 필수입니다.")
    private String promptRequest;
}