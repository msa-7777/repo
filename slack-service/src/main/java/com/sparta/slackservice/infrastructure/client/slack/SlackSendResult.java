package com.sparta.slackservice.infrastructure.client.slack;

import java.time.LocalDateTime;

public record SlackSendResult(

        String channelId,
        String slackTs,
        LocalDateTime sentAt
) {
}