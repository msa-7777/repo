package com.sparta.slackservice.infrastructure.client.slack;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
@Profile({"test"})
public class TemporarySlackClient implements SlackClient {
    // 초기 CRUD 검증 단계에서는 임시 구현체
    // Slack API 구현 전 로컬 CRUD 확인용

    @Override
    public SlackSendResult sendDirectMessage(
            String slackUserId,
            String message
    ) {
        return new SlackSendResult(
                "D-" + UUID.randomUUID(),
                String.valueOf(System.currentTimeMillis()),
                LocalDateTime.now()
        );
    }

    @Override
    public SlackUpdateResult updateMessage(
            String channelId,
            String slackTs,
            String message
    ) {
        return new SlackUpdateResult(
                channelId,
                slackTs
        );
    }
}