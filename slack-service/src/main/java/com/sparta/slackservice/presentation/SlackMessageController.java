package com.sparta.slackservice.presentation;

import com.sparta.slackservice.application.SlackMessageService;
import com.sparta.slackservice.domain.SlackMessageSearchCondition;
import com.sparta.slackservice.domain.SlackMessageStatus;
import com.sparta.slackservice.global.response.RestApiResponse;
import com.sparta.slackservice.presentation.request.SlackMessageCreateRequest;
import com.sparta.slackservice.presentation.request.SlackMessageUpdateRequest;
import com.sparta.slackservice.presentation.response.SlackMessageCreateResponse;
import com.sparta.slackservice.presentation.response.SlackMessageDetailResponse;
import com.sparta.slackservice.presentation.response.SlackMessagePageResponse;
import com.sparta.slackservice.presentation.response.SlackMessageUpdateResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(name = "Slack Message", description = "Slack 메시지 관리 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/slack-messages")
public class SlackMessageController {

    private final SlackMessageService slackMessageService;

    // Slack 메시지 발송
    /*
     * 주문/배송 처리 흐름에서 다음 값이 모두 준비된 상태로 요청한다.
     * - orderId
     * - hubId
     * - receiverId
     * - message
     */
    @Operation(
            summary = "Slack 메시지 발송",
            description = "Slack 메시지를 발송하고 발송 이력을 저장합니다."
    )
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RestApiResponse<SlackMessageCreateResponse> createSlackMessage(
            @Valid @RequestBody SlackMessageCreateRequest request
    ) {
        SlackMessageCreateResponse response = slackMessageService.createSlackMessage(request);

        String message = response.status() == SlackMessageStatus.SENT
                ? "Slack 메시지가 발송되었습니다."
                : "Slack 메시지 발송에 실패하여 실패 이력이 저장되었습니다.";

        return RestApiResponse.success(HttpStatus.CREATED, message, response);
    }

    // Slack 메시지 단건 조회
    // slackUserId, channelId, slackTs, failureReason 같은 상세 연동 정보는 단건 조회 응답에서만 확인한다.
    @Operation(
            summary = "Slack 메시지 단건 조회",
            description = "Slack 메시지 발송 이력을 조회합니다."
    )
    @PreAuthorize("hasRole('MASTER')")
    @GetMapping("/{slackMessageId}")
    public RestApiResponse<SlackMessageDetailResponse> getSlackMessage(
            @PathVariable UUID slackMessageId
    ) {
        SlackMessageDetailResponse response =
                slackMessageService.getSlackMessage(slackMessageId);

        return RestApiResponse.success(
                HttpStatus.OK,
                "Slack 메시지 조회에 성공했습니다.",
                response
        );
    }

    // Slack 메시지 목록 및 검색
    @Operation(
            summary = "Slack 메시지 목록 조회",
            description = "검색 조건에 따라 Slack 메시지 발송 이력을 조회합니다."
    )
    @PreAuthorize("hasRole('MASTER')")
    @GetMapping
    public RestApiResponse<SlackMessagePageResponse> searchSlackMessages(
            @ModelAttribute SlackMessageSearchCondition condition,
            @PageableDefault(page = 0, size = 10) Pageable pageable
    ) {

        SlackMessagePageResponse response =
                slackMessageService.searchSlackMessages(
                        condition,
                        pageable
                );

        return RestApiResponse.success(
                HttpStatus.OK,
                "Slack 메시지 목록 조회에 성공했습니다.",
                response
        );
    }

    // Slack 메시지 수정
    // 수정 가능한 값은 message 하나뿐이다. SENT 또는 MODIFIED 상태의 메시지만 수정할 수 있다.
    @Operation(
            summary = "Slack 메시지 수정",
            description = "발송된 Slack 메시지 내용을 수정합니다."
    )
    @PreAuthorize("hasRole('MASTER')")
    @PatchMapping("/{slackMessageId}")
    public RestApiResponse<SlackMessageUpdateResponse> updateSlackMessage(
            @PathVariable UUID slackMessageId,
            @Valid @RequestBody SlackMessageUpdateRequest request
    ) {

        SlackMessageUpdateResponse response =
                slackMessageService.updateSlackMessage(
                        slackMessageId,
                        request
                );

        return RestApiResponse.success(
                HttpStatus.OK,
                "Slack 메시지가 수정되었습니다.",
                response
        );
    }

    // Slack 메시지 발송 이력 삭제
    // Slack에 이미 발송된 실제 메시지는 삭제하지 않는다.
    @Operation(
            summary = "Slack 메시지 삭제",
            description = "Slack 메시지 발송 이력을 논리 삭제합니다."
    )
    @PreAuthorize("hasRole('MASTER')")
    @DeleteMapping("/{slackMessageId}")
    public RestApiResponse<Void> deleteSlackMessage(
            @PathVariable UUID slackMessageId,
            Authentication authentication
    ) {
        UUID deletedBy =
                UUID.fromString(authentication.getName());

        slackMessageService.deleteSlackMessage(
                slackMessageId,
                deletedBy
        );

        return RestApiResponse.success(
                HttpStatus.OK,
                "Slack 메시지 발송 이력이 삭제되었습니다.",
                null
        );
    }
}
