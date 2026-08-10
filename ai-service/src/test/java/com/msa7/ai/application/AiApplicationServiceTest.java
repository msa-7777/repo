package com.msa7.ai.application;

import com.msa7.ai.domain.model.AiHistory;
import com.msa7.ai.domain.repository.AiHistoryRepository;
import com.msa7.ai.infrastructure.ai.GeminiAiClient;
import com.msa7.ai.infrastructure.client.delivery.DeliveryClient;
import com.msa7.ai.infrastructure.client.delivery.DeliveryResponse;
import com.msa7.ai.infrastructure.client.order.OrderClient;
import com.msa7.ai.infrastructure.client.order.OrderResponse;
import com.msa7.ai.infrastructure.client.product.ProductClient;
import com.msa7.ai.infrastructure.client.product.ProductResponse;
import com.msa7.ai.presentation.dto.request.CreateAiHistoryRequest;
import com.msa7.ai.presentation.dto.response.AiDeadlineResponse;
import com.msa7.ai.presentation.dto.response.AiHistoryResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;


@ExtendWith(MockitoExtension.class)
class AiApplicationServiceTest {

    @Mock
    private OrderClient orderClient;

    @Mock
    private ProductClient productClient;

    @Mock
    private DeliveryClient deliveryClient;

    @Mock
    private GeminiAiClient geminiAiClient;

    @Mock
    private AiHistoryRepository aiHistoryRepository;

    @InjectMocks
    private AiApplicationService aiApplicationService;

    @Test
    @DisplayName("CreateAiHistoryRequest 요청을 받아 각 MSA 서비스 데이터를 조회·조합하고 AI 분석 및 이력 저장을 성공적으로 수행한다")
    void generateDeadlineAndNotify_Success() {
        // given
        UUID orderId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        UUID companyId = UUID.randomUUID();

        CreateAiHistoryRequest request = new CreateAiHistoryRequest(orderId);

        OrderResponse orderResponse = new OrderResponse(
                orderId,
                productId,
                10,
                "배송전 연락 부탁드립니다.",
                UUID.randomUUID(),
                LocalDateTime.now()
        );

        ProductResponse productResponse = new ProductResponse(
                productId,
                "프리미엄 물류 박스",
                companyId
        );

        DeliveryResponse deliveryResponse = new DeliveryResponse(
                UUID.randomUUID(),
                orderId,
                "PENDING",
                UUID.randomUUID(),
                "서울시 강남구 테헤란로 123",
                List.of()
        );

        String mockPrompt = "생성된 프롬프트 내용";

        AiDeadlineResponse mockAiResponse = new AiDeadlineResponse(
                LocalDateTime.now().plusDays(1),
                "AI가 산출한 발송 안내 메시지입니다."
        );

        // Mocking 설정 (슬랙 관련 설정 제외)
        given(orderClient.getOrder(orderId)).willReturn(orderResponse);
        given(productClient.getProduct(productId)).willReturn(productResponse);
        given(deliveryClient.getDeliveryByOrder(orderId)).willReturn(deliveryResponse);
        given(geminiAiClient.buildPrompt(orderResponse, productResponse, deliveryResponse)).willReturn(mockPrompt);
        given(geminiAiClient.getCalculatedDeadline(mockPrompt)).willReturn(mockAiResponse);
        given(aiHistoryRepository.save(any(AiHistory.class))).willAnswer(invocation -> invocation.getArgument(0));

        // when
        AiHistoryResponse result = aiApplicationService.generateDeadlineAndNotify(request);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getGeneratedMessage()).isEqualTo("AI가 산출한 발송 안내 메시지입니다.");

        // 검증: 슬랙을 제외한 MSA 클라이언트 호출 및 레포지토리 저장 여부 확인
        verify(orderClient).getOrder(orderId);
        verify(productClient).getProduct(productId);
        verify(deliveryClient).getDeliveryByOrder(orderId);
        verify(geminiAiClient).buildPrompt(orderResponse, productResponse, deliveryResponse);
        verify(geminiAiClient).getCalculatedDeadline(mockPrompt);
        verify(aiHistoryRepository).save(any(AiHistory.class));
    }
}