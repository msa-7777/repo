package com.msa7.ai.application;

import com.msa7.ai.domain.model.AiHistory;
import com.msa7.ai.domain.repository.AiHistoryRepository;
import com.msa7.ai.global.exception.BusinessException;
import com.msa7.ai.global.exception.ErrorCode;
import com.msa7.ai.infrastructure.ai.GeminiAiClient;
import com.msa7.ai.infrastructure.client.delivery.DeliveryClient;
import com.msa7.ai.infrastructure.client.delivery.DeliveryResponse;
import com.msa7.ai.infrastructure.client.hub.HubRoutePathResponse;
import com.msa7.ai.infrastructure.client.hub.hubClient;
import com.msa7.ai.infrastructure.client.order.OrderClient;
import com.msa7.ai.infrastructure.client.order.OrderResponse;
import com.msa7.ai.infrastructure.client.order.OrderWithDeliveryDto;
import com.msa7.ai.infrastructure.client.product.ProductClient;
import com.msa7.ai.infrastructure.client.product.ProductResponse;
import com.msa7.ai.infrastructure.client.slack.SlackClient;
import com.msa7.ai.presentation.dto.request.CreateAiHistoryRequest;
import com.msa7.ai.presentation.dto.response.AiDeadlineResponse;
import com.msa7.ai.presentation.dto.response.AiHistoryResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
//@Transactional(readOnly = true)
public class AiApplicationService {

    private final GeminiAiClient geminiAiClient;
    private final SlackClient slackClient;
    private final AiHistoryRepository aiHistoryRepository;

    private final OrderClient orderClient;         // Order 서비스 Feign
    private final ProductClient productClient;     // Product 서비스 Feign
    private final DeliveryClient deliveryClient;   // Delivery 서비스 Feign

    private final TransactionTemplate transactionTemplate;
    private final hubClient hubClient;

    /*     #TODO : CreateAiHistoryRequet에 있는
        요청사항(납기일자 및 시간 등), 상품 및 수량정보, 발송지/경유지/도착지 정보, 배송담당자 근무시간 등
        주문쪽에서 가져오는 것 구현 요망   */
    //@Transactional
    public AiHistoryResponse generateDeadlineAndNotify(CreateAiHistoryRequest request) {

        // MSA 서비스 간 동기 통신 (OpenFeign)을 통한 데이터 수집
        OrderResponse order = orderClient.getOrderDetail(request.orderId()).getBody();
        validate(order);

        OrderWithDeliveryDto orderWithDelivery = orderClient.getOrderWithDeliveryStatus(request.orderId()).getBody();
        validate(orderWithDelivery);

        ProductResponse product = productClient.getProduct(order.productId()).getBody().data();
        validate(product);

        DeliveryResponse delivery = deliveryClient.getDeliveryRouteInfo(request.orderId());
        validate(delivery);

        HubRoutePathResponse getRouteInfo = hubClient.getRouteInfo(delivery.startHubId(), delivery.endHubId());
        validate(getRouteInfo);

        // 프롬프트 구성
        String prompt = geminiAiClient.buildPrompt(order, product, delivery, orderWithDelivery, getRouteInfo);

        AiDeadlineResponse aiResponse = null;

        try {
            // Gemini AI 호출 (Structured Output)
            aiResponse = geminiAiClient.getCalculatedDeadline(prompt);

            // AI 메세지 확인
            log.info("calculatedDeadline : " + aiResponse.calculatedDeadline());
            log.info("generatedMessage : " + aiResponse.generatedMessage());

        } catch (Exception e) {
            log.error("AI 답변 생성 실패 : {}", e.getMessage());
            throw new BusinessException(ErrorCode.AI_SERVICE_ERROR);
        }

        // 3. 슬랙 알림 발송
        boolean isNotified = false;
//        try {
//            slackClient.sendNotification(aiResponse.generatedMessage());
//            isNotified = true;
//        } catch (Exception e) {
//            log.error("Slack 메시지 발송 실패: {}", e.getMessage());
//        }

        // AI 이력 저장 (p_ai_histories)
        AiHistory aiHistory = AiHistory.create(
                request.orderId(),
                prompt,
                aiResponse.calculatedDeadline(),
                aiResponse.generatedMessage(),
                isNotified
        );

        // [트랜잭션 안] DB 저장만 트랜잭션 블록으로 감싸기
        AiHistory savedHistory = transactionTemplate.execute(status ->
                aiHistoryRepository.save(aiHistory)
        );

        if (savedHistory == null) {
            throw new BusinessException(ErrorCode.AI_SERVICE_ERROR, "AI 메시지를 찾을 수 없습니다.");
        }

        return AiHistoryResponse.from(savedHistory);
    }

    @Transactional(readOnly = true)
    public AiHistoryResponse getAiHistory(UUID historyId) {
        AiHistory aiHistory = aiHistoryRepository.findByHistoryIdAndDeletedAtIsNull(historyId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "AI 분석 이력을 찾을 수 없습니다."));
        return AiHistoryResponse.from(aiHistory);
    }

    @Transactional(readOnly = true)
    public Page<AiHistoryResponse> searchAiHistories(UUID orderId, Pageable pageable) {
        return aiHistoryRepository.searchAiHistories(orderId, pageable)
                .map(AiHistoryResponse::from);
    }

    @Transactional
    public void deleteAiHistory(UUID historyId, UUID deletedBy) {
        AiHistory aiHistory = aiHistoryRepository.findByHistoryIdAndDeletedAtIsNull(historyId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "AI 분석 이력을 찾을 수 없습니다."));
        aiHistory.delete(deletedBy);
    }


    private void validate(Object obj) {
        if (obj == null) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND);
        }
    }

}