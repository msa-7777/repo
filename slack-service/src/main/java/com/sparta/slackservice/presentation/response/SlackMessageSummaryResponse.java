package com.sparta.slackservice.presentation.response;

import com.sparta.slackservice.domain.SlackMessage;
import com.sparta.slackservice.domain.SlackMessageStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record SlackMessageSummaryResponse(

        UUID slackMessageId,
        UUID orderId,
        UUID hubId,
        UUID receiverId,
        String receiverName,
        String message,
        SlackMessageStatus status,
        LocalDateTime sentAt,
        LocalDateTime createdAt
) {

    public static SlackMessageSummaryResponse from(
            SlackMessage slackMessage
    ) {
        return new SlackMessageSummaryResponse(
                slackMessage.getId(),
                slackMessage.getOrderId(),
                slackMessage.getHubId(),
                slackMessage.getReceiverId(),
                slackMessage.getReceiverName(),
                slackMessage.getMessage(),
                slackMessage.getStatus(),
                slackMessage.getSentAt(),
                slackMessage.getCreatedAt()
        );
    }
}