package com.msa7.ai.presentation.controller;

import com.msa7.ai.application.AiApplicationService;
import com.msa7.ai.global.response.RestApiResponse;
import com.msa7.ai.presentation.dto.request.CreateAiHistoryRequest;
import com.msa7.ai.presentation.dto.response.AiHistoryResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(name = "AI History API", description = "AI 분석 및 이력 관리 API")
@RestController
@RequestMapping("/api/v1/ai")
@RequiredArgsConstructor
public class AiController {

    private static final String ROLE_PREFIX = "ROLE_";
    private final AiApplicationService aiApplicationService;

    @Operation(summary = "AI 납기일 계산 및 Slack 알림 발송", description = "주문, 상품, 배송, 허브 경로 정보를 조합하여 AI 분석 메시지를 생성하고 Slack으로 발송합니다.")
    @PreAuthorize("hasAnyRole()")
    @PostMapping("/generate")
    public ResponseEntity<RestApiResponse<AiHistoryResponse>> generateDeadline(
            @Valid @RequestBody CreateAiHistoryRequest request) {
        AiHistoryResponse response = aiApplicationService.generateDeadlineAndNotify(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(RestApiResponse.ok(HttpStatus.CREATED, "AI 최종 발송 시한 생성 및 메시지 전송 성공", response));
    }

    @Operation(summary = "AI 분석 이력 단건 조회", description = "AI 분석 이력을 조회합니다.")
    @PreAuthorize("hasAnyRole('MASTER')")
    @GetMapping("/{historyId}")
    public ResponseEntity<RestApiResponse<AiHistoryResponse>> getAiHistory(
            @PathVariable UUID historyId) {
        AiHistoryResponse response = aiApplicationService.getAiHistory(historyId);
        return ResponseEntity.ok(RestApiResponse.ok(HttpStatus.OK, "AI 분석 이력 조회 성공", response));
    }

    @Operation(summary = "AI 분석 이력 목록 검색", description = "AI 분석 이력 목록을 조회합니다.")
    @PreAuthorize("hasAnyRole('MASTER')")
    @GetMapping
    public ResponseEntity<RestApiResponse<Page<AiHistoryResponse>>> searchAiHistories(
            @RequestParam(required = false) UUID orderId,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<AiHistoryResponse> response = aiApplicationService.searchAiHistories(orderId, pageable);
        return ResponseEntity.ok(RestApiResponse.ok(HttpStatus.OK, "AI 분석 이력 목록 조회 성공", response));
    }

    @Operation(summary = "AI 분석 이력 삭제", description = "AI 이력을 논리 삭제합니다.")
    @PreAuthorize("hasAnyRole('MASTER')")
    @DeleteMapping("/{historyId}")
    public ResponseEntity<RestApiResponse<Void>> deleteAiHistory(
            @PathVariable UUID historyId,
            Authentication authentication) {
        UUID userId = getUserId(authentication);
        aiApplicationService.deleteAiHistory(historyId, userId);
        return ResponseEntity.ok(RestApiResponse.ok(HttpStatus.OK, "AI 이력이 정상 삭제(Soft Delete) 되었습니다.", null));
    }

    // --- Helper Methods ---
    private UUID getUserId(Authentication authentication) {
        return UUID.fromString(authentication.getName());
    }

    private String getRole(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(authority -> authority.startsWith(ROLE_PREFIX))
                .findFirst()
                .map(authority -> authority.substring(ROLE_PREFIX.length()))
                .orElseThrow(() -> new IllegalArgumentException("권한 정보를 찾을 수 없습니다."));
    }
}