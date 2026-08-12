package com.msa7.ai.infrastructure.client.slack;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;
import java.util.UUID;

public record SlackMessageCreateRequest(
        UUID orderId,
        UUID hubId,
        UUID receiverId,
        String message,

        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
        LocalDateTime calculatedDeadline,
        String generatedMessage
) {
        public SlackMessageCreateRequest createMessage(SlackMessageCreateRequest setMessage) {
                return new SlackMessageCreateRequest(
                        this.orderId, this.hubId, this.receiverId, this.message, this.calculatedDeadline, this.generatedMessage);
        }
}