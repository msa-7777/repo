package com.sparta.slackservice.global.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;

@Configuration
@Profile({"local", "dev"})
public class SlackClientConfig {

    /**
     * Slack Web API 호출에 공통으로 사용할 RestClient를 생성한다.
     *
     * baseUrl:
     * - Slack Web API의 공통 주소
     *
     * Authorization:
     * - Slack App에서 발급한 Bot User OAuth Token
     * - Bearer 인증 방식으로 모든 요청에 자동 포함
     *
     * Content-Type:
     * - Slack API에 JSON 요청 본문을 전달
     */
    @Bean
    public RestClient slackRestClient(
            @Value("${slack.api.base-url}") String baseUrl,
            @Value("${slack.api.bot-token}") String botToken
    ) {
        return RestClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + botToken)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }
}