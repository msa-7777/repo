package com.msa7.ai.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

import java.time.LocalDateTime;
import java.util.List;


public record CreateAiHistoryRequest(
        @NotNull(message = "주문 ID는 필수입니다.") UUID orderId
//        @NotBlank(message = "주문자 정보는 필수입니다.") String ordererInfo,
//        @NotNull(message = "주문 시간은 필수입니다.") LocalDateTime orderTime,
//        @NotBlank(message = "상품명은 필수입니다.") String productName,
//        @NotNull(message = "수량은 필수입니다.") Integer quantity,
//        @NotBlank(message = "요청사항은 필수입니다.") String requestDetails,
//        @NotBlank(message = "발송지는 필수입니다.") String originHub,
//        List<String> transitHubs,
//        @NotBlank(message = "도착지는 필수입니다.") String destinationHub,
//        @NotBlank(message = "배송담당자 정보는 필수입니다.") String deliveryManagerInfo
) {}