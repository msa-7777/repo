package com.sparta.slackservice.presentation.response;

import com.sparta.slackservice.domain.SlackMessage;
import com.sparta.slackservice.domain.SlackMessageStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record SlackMessageDetailResponse(
        // 단건 조회

        UUID slackMessageId,
        UUID orderId,
        UUID hubId,
        UUID receiverId,
        String receiverName,
        String slackUserId,
        String message,
        SlackMessageStatus status,
        String failureReason,
        String channelId,
        String slackTs,
        LocalDateTime sentAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

    public static SlackMessageDetailResponse from(
            SlackMessage slackMessage
    ) {
        return new SlackMessageDetailResponse(
                slackMessage.getId(),
                slackMessage.getOrderId(),
                slackMessage.getHubId(),
                slackMessage.getReceiverId(),
                slackMessage.getReceiverName(),
                slackMessage.getSlackUserId(),
                slackMessage.getMessage(),
                slackMessage.getStatus(),
                slackMessage.getFailureReason(),
                slackMessage.getChannelId(),
                slackMessage.getSlackTs(),
                slackMessage.getSentAt(),
                slackMessage.getCreatedAt(),
                slackMessage.getUpdatedAt()
        );
    }
}
