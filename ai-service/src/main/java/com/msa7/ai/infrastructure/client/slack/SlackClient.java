package com.msa7.ai.infrastructure.client.slack;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

@FeignClient(name = "slack-service")
public interface SlackClient {

    @PostMapping("/api/v1/slack-messages") // SlackMessageController의 매핑 URL
    void sendNotification(@RequestBody SlackMessageCreateRequest request);
}