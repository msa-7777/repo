package com.msa7.ai.application;

import com.msa7.ai.domain.model.AiHistory;
import com.msa7.ai.domain.repository.AiHistoryRepository;
import com.msa7.ai.global.common.BusinessException;
import com.msa7.ai.global.common.ErrorCode;
import com.msa7.ai.infrastructure.ai.GeminiAiClient;
import com.msa7.ai.infrastructure.client.SlackClient;
import com.msa7.ai.presentation.dto.request.CreateAiHistoryRequest;
import com.msa7.ai.presentation.dto.response.AiDeadlineResponse;
import com.msa7.ai.presentation.dto.response.AiHistoryResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AiApplicationService {

    private final GeminiAiClient geminiAiClient;
    private final SlackClient slackClient;
    private final AiHistoryRepository aiHistoryRepository;

/*     #TODO : CreateAiHistoryRequet에 있는
    요청사항(납기일자 및 시간 등), 상품 및 수량정보, 발송지/경유지/도착지 정보, 배송담당자 근무시간 등
    주문쪽에서 가져오는 것 구현 요망   */
    @Transactional
    public AiHistoryResponse generateDeadlineAndNotify(CreateAiHistoryRequest request) {
        // 1. 프롬프트 구성
        String prompt = geminiAiClient.buildPrompt(request);

        // 2. Gemini AI 호출 (Structured Output)
        AiDeadlineResponse aiResponse = geminiAiClient.getCalculatedDeadline(prompt);

        //AI 메세지 확인
        log.info("calculatedDeadline : " + aiResponse.calculatedDeadline());
        log.info("generatedMessage : " + aiResponse.generatedMessage());

        // 3. 슬랙 알림 발송
        boolean isNotified = false;
//        try {
//            slackClient.sendNotification(aiResponse.generatedMessage());
//            isNotified = true;
//        } catch (Exception e) {
//            log.error("Slack 메시지 발송 실패: {}", e.getMessage());
//        }

        // 4. AI 이력 저장 (p_ai_histories)
        AiHistory aiHistory = AiHistory.create(
                request.orderId(),
                prompt,
                aiResponse.calculatedDeadline(),
                aiResponse.generatedMessage(),
                isNotified
        );

        AiHistory savedHistory = aiHistoryRepository.save(aiHistory);
        return AiHistoryResponse.from(savedHistory);
    }

    public AiHistoryResponse getAiHistory(UUID historyId) {
        AiHistory aiHistory = aiHistoryRepository.findByHistoryIdAndDeletedAtIsNull(historyId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "AI 분석 이력을 찾을 수 없습니다."));
        return AiHistoryResponse.from(aiHistory);
    }

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
}