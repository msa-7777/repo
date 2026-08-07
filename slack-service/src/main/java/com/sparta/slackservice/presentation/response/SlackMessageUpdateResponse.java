package com.sparta.slackservice.presentation.response;


import com.sparta.slackservice.domain.SlackMessage;
import com.sparta.slackservice.domain.SlackMessageStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record SlackMessageUpdateResponse(

        UUID slackMessageId,
        String message,
        SlackMessageStatus status,
        LocalDateTime updatedAt
) {

    public static SlackMessageUpdateResponse from(
            SlackMessage slackMessage
    ) {
        return new SlackMessageUpdateResponse(
                slackMessage.getId(),
                slackMessage.getMessage(),
                slackMessage.getStatus(),
                slackMessage.getUpdatedAt()
        );
    }
}