package com.msa7.ai.infrastructure.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

@FeignClient(name = "slackClient", url = "${slack.webhook-url}")
public interface SlackClient {

    @PostMapping
    void sendSlackWebhook(@RequestBody Map<String, Object> body);

    default void sendNotification(String message) {
        sendSlackWebhook(Map.of("text", message));
    }
}