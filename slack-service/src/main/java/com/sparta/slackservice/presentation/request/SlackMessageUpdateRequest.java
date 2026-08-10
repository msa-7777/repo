package com.sparta.slackservice.presentation.request;

import jakarta.validation.constraints.NotBlank;

public record SlackMessageUpdateRequest(

        @NotBlank(message = "메시지는 필수입니다.")
        String message
) {
}