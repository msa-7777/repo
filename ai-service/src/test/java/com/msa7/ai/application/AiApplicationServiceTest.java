package com.msa7.ai.application;

import com.msa7.ai.domain.model.AiHistory;
import com.msa7.ai.domain.repository.AiHistoryRepository;
import com.msa7.ai.global.response.RestApiResponse;
import com.msa7.ai.infrastructure.ai.GeminiAiClient;
import com.msa7.ai.infrastructure.client.delivery.DeliveryClient;
import com.msa7.ai.infrastructure.client.delivery.DeliveryResponse;
import com.msa7.ai.infrastructure.client.hub.HubRoutePathResponse;
import com.msa7.ai.infrastructure.client.hub.HubClient;
import com.msa7.ai.infrastructure.client.order.OrderClient;
import com.msa7.ai.infrastructure.client.order.OrderResponse;
import com.msa7.ai.infrastructure.client.order.OrderStatus;
import com.msa7.ai.infrastructure.client.order.OrderWithDeliveryDto;
import com.msa7.ai.infrastructure.client.product.ProductClient;
import com.msa7.ai.infrastructure.client.product.ProductResponse;
import com.msa7.ai.infrastructure.client.slack.SlackClient;
import com.msa7.ai.infrastructure.client.slack.SlackMessageCreateRequest;
import com.msa7.ai.presentation.dto.request.CreateAiHistoryRequest;
import com.msa7.ai.presentation.dto.response.AiDeadlineResponse;
import com.msa7.ai.presentation.dto.response.AiHistoryResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AiApplicationServiceTest {

    @Mock
    private GeminiAiClient geminiAiClient;

    @Mock
    private AiHistoryRepository aiHistoryRepository;

    @Mock
    private SlackClient slackClient;

    @Mock
    private OrderClient orderClient;

    @Mock
    private ProductClient productClient;

    @Mock
    private DeliveryClient deliveryClient;

    @Mock
    private HubClient hubClient;

    @Mock
    private TransactionTemplate transactionTemplate;

    @InjectMocks
    private AiApplicationService aiApplicationService;

    @Test
    @DisplayName("CreateAiHistoryRequest 요청을 받아 각 MSA 서비스 데이터를 조회·조합하고 AI 분석, Slack 알림, 이력 저장을 성공적으로 수행한다")
    void generateDeadlineAndNotify_Success() {
        // given
        UUID orderId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        UUID companyId = UUID.randomUUID();
        UUID deliveryId = UUID.randomUUID();
        UUID startHubId = UUID.randomUUID();
        UUID endHubId = UUID.randomUUID();
        UUID companyManagerId = UUID.randomUUID();
        OrderStatus status = OrderStatus.DELIVERED;
        UUID receiverCompanyId = UUID.randomUUID();

        CreateAiHistoryRequest request = new CreateAiHistoryRequest(orderId);

        OrderResponse orderResponse = new OrderResponse(
                orderId,
                productId,
                10,
                "배송전 연락 부탁드립니다.",
                status,
                receiverCompanyId
        );

        OrderWithDeliveryDto orderWithDeliveryDto = new OrderWithDeliveryDto(
                orderId,
                status.name(),
                "PENDING",
                deliveryId
        );

        ProductResponse productResponse = new ProductResponse(
                productId,
                "프리미엄 물류 박스",
                companyId
        );

        // DeliveryResponse (6개 필드: deliveryId, startHubId, endHubId, destinationAddress, companyManagerId, routeRecords)
        DeliveryResponse deliveryResponse = new DeliveryResponse(
                deliveryId,
                startHubId,
                endHubId,
                "서울시 강남구 테헤란로 123", // destinationAddress (String)
                companyManagerId,              // companyManagerId (UUID)
                List.of()                      // routeRecords (List)
        );

        // HubRoutePathResponse (3개 필드: totalDistance, totalDuration, segments)
        HubRoutePathResponse hubRoutePathResponse = new HubRoutePathResponse(
                10,
                30,
                List.of()
        );

        String mockPrompt = "생성된 프롬프트 내용";

        AiDeadlineResponse mockAiResponse = new AiDeadlineResponse(
                LocalDateTime.now().plusDays(1),
                "AI가 산출한 발송 안내 메시지입니다."
        );

        // OpenFeign Mocking
        given(orderClient.getOrderDetail(orderId)).willReturn(ResponseEntity.ok(orderResponse));
        given(orderClient.getOrderWithDeliveryStatus(orderId)).willReturn(ResponseEntity.ok(orderWithDeliveryDto));

        RestApiResponse<ProductResponse> productRestResponse = RestApiResponse.ok(HttpStatus.OK, "성공", productResponse);
        given(productClient.getProduct(productId)).willReturn(ResponseEntity.ok(productRestResponse));

        given(deliveryClient.getDeliveryRouteInfo(orderId)).willReturn(deliveryResponse);
        given(hubClient.getRouteInfo(startHubId, endHubId)).willReturn(hubRoutePathResponse);

        // AI Mocking
        given(geminiAiClient.buildPrompt(orderResponse, productResponse, deliveryResponse, orderWithDeliveryDto, hubRoutePathResponse))
                .willReturn(mockPrompt);
        given(geminiAiClient.getCalculatedDeadline(mockPrompt)).willReturn(mockAiResponse);

        // TransactionTemplate Mocking
        given(transactionTemplate.execute(any())).willAnswer(invocation -> {
            TransactionCallback<?> action = invocation.getArgument(0);
            return action.doInTransaction(null);
        });

        // Repository Mocking
        given(aiHistoryRepository.save(any(AiHistory.class))).willAnswer(invocation -> invocation.getArgument(0));

        // when
        AiHistoryResponse result = aiApplicationService.generateDeadlineAndNotify(request);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getGeneratedMessage()).isEqualTo("AI가 산출한 발송 안내 메시지입니다.");

        // 검증
        verify(orderClient).getOrderDetail(orderId);
        verify(orderClient).getOrderWithDeliveryStatus(orderId);
        verify(productClient).getProduct(productId);
        verify(deliveryClient).getDeliveryRouteInfo(orderId);
        verify(hubClient).getRouteInfo(startHubId, endHubId);
        verify(geminiAiClient).buildPrompt(orderResponse, productResponse, deliveryResponse, orderWithDeliveryDto, hubRoutePathResponse);
        verify(geminiAiClient).getCalculatedDeadline(mockPrompt);
        verify(slackClient).sendNotification(any(SlackMessageCreateRequest.class));
        verify(aiHistoryRepository).save(any(AiHistory.class));
    }

    @Test
    @DisplayName("Slack 메시지 발송에 실패하더라도 AI 분석 이력은 정상적으로 DB에 저장되어야 한다")
    void generateDeadlineAndNotify_SlackFailure_StillSavesHistory() {
        // given
        UUID orderId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        UUID deliveryId = UUID.randomUUID();
        UUID startHubId = UUID.randomUUID();
        UUID endHubId = UUID.randomUUID();
        UUID companyManagerId = UUID.randomUUID();

        CreateAiHistoryRequest request = new CreateAiHistoryRequest(orderId);

        OrderResponse orderResponse = new OrderResponse(orderId, productId, 10, "요청사항", OrderStatus.PENDING, UUID.randomUUID());
        OrderWithDeliveryDto orderWithDeliveryDto = new OrderWithDeliveryDto(orderId, "PENDING", "PENDING", deliveryId);
        ProductResponse productResponse = new ProductResponse(productId, "상품명", UUID.randomUUID());

        DeliveryResponse deliveryResponse = new DeliveryResponse(
                deliveryId,
                startHubId,
                endHubId,
                "주소",
                companyManagerId,
                List.of()
        );

        HubRoutePathResponse hubRoutePathResponse = new HubRoutePathResponse(10, 30, List.of());

        String mockPrompt = "생성된 프롬프트 내용";
        AiDeadlineResponse mockAiResponse = new AiDeadlineResponse(LocalDateTime.now().plusDays(1), "AI 메시지");

        given(orderClient.getOrderDetail(orderId)).willReturn(ResponseEntity.ok(orderResponse));
        given(orderClient.getOrderWithDeliveryStatus(orderId)).willReturn(ResponseEntity.ok(orderWithDeliveryDto));

        RestApiResponse<ProductResponse> productRestResponse = RestApiResponse.ok(HttpStatus.OK, "성공", productResponse);
        given(productClient.getProduct(productId)).willReturn(ResponseEntity.ok(productRestResponse));

        given(deliveryClient.getDeliveryRouteInfo(orderId)).willReturn(deliveryResponse);
        given(hubClient.getRouteInfo(startHubId, endHubId)).willReturn(hubRoutePathResponse);

        given(geminiAiClient.buildPrompt(orderResponse, productResponse, deliveryResponse, orderWithDeliveryDto, hubRoutePathResponse)).willReturn(mockPrompt);
        given(geminiAiClient.getCalculatedDeadline(mockPrompt)).willReturn(mockAiResponse);

        // Slack 발송 예외 발생
        doThrow(new RuntimeException("Slack 서버 오류")).when(slackClient).sendNotification(any(SlackMessageCreateRequest.class));

        given(transactionTemplate.execute(any())).willAnswer(invocation -> {
            TransactionCallback<?> action = invocation.getArgument(0);
            return action.doInTransaction(null);
        });
        given(aiHistoryRepository.save(any(AiHistory.class))).willAnswer(invocation -> invocation.getArgument(0));

        // when
        AiHistoryResponse result = aiApplicationService.generateDeadlineAndNotify(request);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getGeneratedMessage()).isEqualTo("AI 메시지");
        verify(aiHistoryRepository).save(any(AiHistory.class));
    }
}