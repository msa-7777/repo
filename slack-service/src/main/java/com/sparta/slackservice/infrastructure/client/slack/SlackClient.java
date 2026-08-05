package com.sparta.slackservice.infrastructure.client.slack;

public interface SlackClient {

    SlackSendResult sendDirectMessage(
            String slackUserId,
            String message
    );

    SlackUpdateResult updateMessage(
            String channelId,
            String slackTs,
            String message
    );
}