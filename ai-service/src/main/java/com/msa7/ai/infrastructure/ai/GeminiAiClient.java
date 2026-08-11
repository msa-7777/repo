package com.msa7.ai.infrastructure.ai;

import com.msa7.ai.infrastructure.client.delivery.DeliveryResponse;
import com.msa7.ai.infrastructure.client.order.OrderResponse;
import com.msa7.ai.infrastructure.client.order.OrderWithDeliveryDto;
import com.msa7.ai.infrastructure.client.product.ProductResponse;
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

    public String buildPrompt(OrderResponse order, ProductResponse product, DeliveryResponse delivery, OrderWithDeliveryDto orderWithDelivery) {

        // 경유지(routeRecords) 포맷팅
        String routeStr = (delivery.routeRecords() == null || delivery.routeRecords().isEmpty())
                ? "직송 (경유지 없음)"
                : delivery.routeRecords().stream()
                .map(r -> String.format("[순서:%d, 출발:%s -> 도착:%s, 도착지:%s", //, 소요:%d분]
                        r.sequence(), delivery.startHubId(), delivery.endHubId(), delivery.destinationAddress()))   //r.estimatedTime()
                .reduce((a, b) -> a + ", " + b).orElse("");

        return String.format("""
                [주문 분석 요청 데이터]
                - 주문번호: %s
                - 상품명: %s (수량: %d개)
                - 요청사항(납기일자 등): %s
                - 배송상태: %s
                - 도착지 주소: %s
                - 배송 경유지 정보: %s
                """,
                order.orderId(),
                product.name(),       // ProductResponse의 필드명인 name 사용
                order.quantity(),
                order.requestNotes(),
                orderWithDelivery.deliveryStatus(),
                delivery.destinationAddress(),
                routeStr
        );
    }

    public AiDeadlineResponse getCalculatedDeadline(String prompt) {
        return geminiChatClient.prompt()
                .user(prompt)
                .call()
                .entity(AiDeadlineResponse.class); // 응답을 자바 객체로 맵핑 (Structured Output)
    }
}