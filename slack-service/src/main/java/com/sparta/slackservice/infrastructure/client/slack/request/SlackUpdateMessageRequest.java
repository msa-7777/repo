package com.sparta.slackservice.infrastructure.client.slack.request;

public record SlackUpdateMessageRequest(
        // 수정 요청

        String channel,
        String ts,
        String text
) {
}