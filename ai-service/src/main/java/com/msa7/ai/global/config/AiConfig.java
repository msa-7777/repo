package com.msa7.ai.global.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AiConfig {

    /**
     * Spring AI가 자동 설정한 ChatClient.Builder를 주입받아
     * 공통 페르소나(System Prompt) 및 기본 옵션이 적용된 ChatClient 빈을 생성합니다.
     */
    @Bean
    public ChatClient geminiChatClient(ChatClient.Builder builder) {
        return builder
                .defaultSystem("""
                    너는 물류 배송 시스템의 전문 AI 스케줄러야.
                    제공된 주문 정보를 정밀 분석하여 최종 발송 시한과 슬랙 알림 메시지를 생성해줘.
                    """)
                .build();
    }
}