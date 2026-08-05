package com.msa7.ai.presentation.controller;

import com.msa7.ai.application.AiApplicationService;
import com.msa7.ai.global.common.RestApiResponse;
import com.msa7.ai.presentation.dto.request.AiCalculationRequest;
import com.msa7.ai.presentation.dto.response.AiCalculationResponse;
import com.msa7.ai.presentation.dto.response.AiHistoryResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/ai")
@RequiredArgsConstructor
public class AiController {

    private final AiApplicationService aiApplicationService;

    // 1. Gemini AI를 활용한 발송 마감시한 계산 및 슬랙 메시지 생성 이력 저장
    @PostMapping("/calculate")
    public ResponseEntity<RestApiResponse<AiCalculationResponse>> calculateDeadline(
            @Valid @RequestBody AiCalculationRequest request
    ) {
        AiCalculationResponse response = aiApplicationService.calculateDeadlineAndGenerateMessage(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(RestApiResponse.ok(HttpStatus.CREATED, "AI 발송 시한 계산 및 메시지 생성이 완료되었습니다.", response));
    }

}