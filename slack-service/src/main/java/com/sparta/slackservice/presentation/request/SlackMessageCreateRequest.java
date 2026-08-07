package com.sparta.slackservice.presentation.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record SlackMessageCreateRequest(

        @NotNull(message = "주문 ID는 필수입니다.")
        UUID orderId,

        @NotNull(message = "허브 ID는 필수입니다.")
        UUID hubId,

        @NotNull(message = "수신자 ID는 필수입니다.")
        UUID receiverId,

        @NotBlank(message = "메시지는 필수입니다.")
        String message
) {
}