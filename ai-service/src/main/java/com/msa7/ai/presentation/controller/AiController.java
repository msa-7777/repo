package com.msa7.ai.presentation.controller;

import com.msa7.ai.application.AiApplicationService;
import com.msa7.ai.global.response.RestApiResponse;
import com.msa7.ai.presentation.dto.request.CreateAiHistoryRequest;
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

    @PostMapping("/generate")
    public ResponseEntity<RestApiResponse<AiHistoryResponse>> generateDeadline(
            @Valid @RequestBody CreateAiHistoryRequest request) {
        AiHistoryResponse response = aiApplicationService.generateDeadlineAndNotify(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(RestApiResponse.ok(HttpStatus.CREATED, "AI 최종 발송 시한 생성 및 메시지 전송 성공", response));
    }

    @GetMapping("/{historyId}")
    public ResponseEntity<RestApiResponse<AiHistoryResponse>> getAiHistory(
            @PathVariable UUID historyId) {
        AiHistoryResponse response = aiApplicationService.getAiHistory(historyId);
        return ResponseEntity.ok(RestApiResponse.ok(HttpStatus.OK, "AI 분석 이력 조회 성공", response));
    }

    @GetMapping
    public ResponseEntity<RestApiResponse<Page<AiHistoryResponse>>> searchAiHistories(
            @RequestParam(required = false) UUID orderId,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<AiHistoryResponse> response = aiApplicationService.searchAiHistories(orderId, pageable);
        return ResponseEntity.ok(RestApiResponse.ok(HttpStatus.OK, "AI 분석 이력 목록 조회 성공", response));
    }

    @DeleteMapping("/{historyId}")
    public ResponseEntity<RestApiResponse<Void>> deleteAiHistory(
            @PathVariable UUID historyId,
            @RequestHeader(value = "X-User-Id", required = false) UUID userId) {
        UUID deletedBy = (userId != null) ? userId : UUID.fromString("11111111-1111-1111-1111-111111111111");
        aiApplicationService.deleteAiHistory(historyId, deletedBy);
        return ResponseEntity.ok(RestApiResponse.ok(HttpStatus.OK, "AI 이력이 정상 삭제(Soft Delete) 되었습니다.", null));
    }
}