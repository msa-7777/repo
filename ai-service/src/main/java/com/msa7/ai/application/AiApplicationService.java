package com.msa7.ai.application;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.msa7.ai.domain.model.AiHistory;
import com.msa7.ai.domain.repository.AiHistoryRepository;
import com.msa7.ai.global.common.BusinessException;
import com.msa7.ai.global.common.ErrorCode;
import com.msa7.ai.presentation.dto.request.AiCalculationRequest;
import com.msa7.ai.presentation.dto.response.AiCalculationResponse;
import com.msa7.ai.presentation.dto.response.AiHistoryResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AiApplicationService {

    private final ChatModel chatModel;
    private final AiHistoryRepository aiHistoryRepository;
    private final ObjectMapper objectMapper;

    // #TODO 슬랙 메시지 전송 관련 수정 요망
    // #TODO 주문 요청사항(납기일자 및 시간 등), 발송지/경유지/도착지 정보, 배송 담당자 근무시간(09-18) 추가 요망 -> order쪽 연동 필요
    @Transactional
    public AiCalculationResponse calculateDeadlineAndGenerateMessage(AiCalculationRequest request) {
        try {
            // 1. Gemini AI에 전달할 구조화된 프롬프트 구성
            String prompt = String.format("""
                    당신은 B2B 물류 시스템의 배송 시한 계산 및 슬랙 알림 생성 AI입니다.
                    아래 주문 정보와 배송 정보를 분석하여 최종 발송 마감 시한과 Slack 메시지를 작성하세요.
                    
                    [주문 정보]
                    - 주문 ID: %s
                    - 요청사항: %s
                    
                    [응답 형식]
                    반드시 아래 JSON 형태로만 응답하세요:
                    {
                      "calculatedDeadline": "YYYY-MM-DDTHH:mm:ss",
                      "generatedMessage": "슬랙에 전송할 알림 메시지 전문"
                    }
                    """, request.getOrderId(), request.getPromptRequest());

            // 2. Spring AI 모델 호출
            String aiResultText = chatModel.call(prompt);

            // 3. AI 응답 JSON 파싱
            JsonNode rootNode = objectMapper.readTree(extractJson(aiResultText));
            String deadlineStr = rootNode.path("calculatedDeadline").asText();
            String message = rootNode.path("generatedMessage").asText();

            LocalDateTime calculatedDeadline = LocalDateTime.parse(deadlineStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME);

            // 4. p_ai_histories 테이블에 이력 저장
            AiHistory history = AiHistory.builder()
                    .orderId(request.getOrderId())
                    .promptRequest(request.getPromptRequest())
                    .calculatedDeadline(calculatedDeadline)
                    .generatedMessage(message)
                    .isSlackNotified(false) // 슬랙 전송 전 초기값
                    .build();

            AiHistory savedHistory = aiHistoryRepository.save(history);

            return AiCalculationResponse.from(savedHistory);

        } catch (Exception e) {
            log.error("AI 배송 시한 계산 및 메시지 생성 실패: {}", e.getMessage(), e);
            throw new BusinessException(ErrorCode.AI_SERVICE_ERROR);
        }
    }

    public AiHistoryResponse getAiHistory(UUID historyId) {
        AiHistory history = aiHistoryRepository.findByHistoryIdAndDeletedAtIsNull(historyId)
                .orElseThrow(() -> new BusinessException(ErrorCode.AI_LOG_NOT_FOUND));
        return AiHistoryResponse.from(history);
    }

    public Page<AiHistoryResponse> getAiHistories(UUID orderId, Pageable pageable) {
        Page<AiHistory> histories;
        if (orderId != null) {
            histories = aiHistoryRepository.findAllByOrderIdAndDeletedAtIsNull(orderId, pageable);
        } else {
            histories = aiHistoryRepository.findAllByDeletedAtIsNull(pageable);
        }
        return histories.map(AiHistoryResponse::from);
    }


    // 마크다운 형태의 ```json ... ``` 텍스트 제거 헬퍼
    private String extractJson(String raw) {
        if (raw.contains("```json")) {
            return raw.substring(raw.indexOf("```json") + 7, raw.lastIndexOf("```")).trim();
        } else if (raw.contains("```")) {
            return raw.substring(raw.indexOf("```") + 3, raw.lastIndexOf("```")).trim();
        }
        return raw.trim();
    }

}