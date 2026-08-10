package com.sparta.slackservice.presentation.response;

import com.sparta.slackservice.domain.SlackMessage;
import com.sparta.slackservice.domain.SlackMessageStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record SlackMessageCreateResponse(
        UUID slackMessageId,
        SlackMessageStatus status,
        LocalDateTime sentAt
) {

    public static SlackMessageCreateResponse from(SlackMessage slackMessage) {
        return new SlackMessageCreateResponse(
                slackMessage.getId(),
                slackMessage.getStatus(),
                slackMessage.getSentAt()
        );
    }
}