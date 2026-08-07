package com.sparta.slackservice.infrastructure.client.slack.response;

public record SlackMessageResponse(
        // Slack API 응답 DTO
        // Slack 외부 API 응답 구조
        boolean ok,
        String channel,
        String ts,
        String error
) {
}
