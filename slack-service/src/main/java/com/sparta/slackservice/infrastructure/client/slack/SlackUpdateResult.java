package com.sparta.slackservice.infrastructure.client.slack;

public record SlackUpdateResult(

        String channelId,
        String slackTs
) {
}