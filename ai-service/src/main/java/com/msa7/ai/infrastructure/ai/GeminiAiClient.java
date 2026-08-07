package com.msa7.ai.infrastructure.ai;

import com.msa7.ai.presentation.dto.request.CreateAiHistoryRequest;
import com.msa7.ai.presentation.dto.response.AiDeadlineResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GeminiAiClient {

    // Builder가 아닌, AiConfig에서 생성한 ChatClient 빈을 직접 주입받습니다.
    private final ChatClient geminiChatClient;

    public String buildPrompt(CreateAiHistoryRequest req) {
        return String.format("""
            [주문 분석 요청 데이터]
            - 주문번호: %s
            - 주문자 정보: %s
            - 주문시간: %s
            - 상품 정보: %s %d박스
            - 요청사항: %s
            - 발송지: %s
            - 경유지: %s
            - 도착지: %s
            - 배송담당자: %s
            """,
                req.orderId(), req.ordererInfo(), req.orderTime(),
                req.productName(), req.quantity(), req.requestDetails(),
                req.originHub(),
                (req.transitHubs() != null && !req.transitHubs().isEmpty()) ? String.join(", ", req.transitHubs()) : "직송(경유지 없음)",
                req.destinationHub(), req.deliveryManagerInfo());
    }

    public AiDeadlineResponse getCalculatedDeadline(String prompt) {
        return geminiChatClient.prompt()
                .user(prompt)
                .call()
                .entity(AiDeadlineResponse.class); // 응답을 자바 객체로 맵핑 (Structured Output)
    }
}