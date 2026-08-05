package com.sparta.slackservice.presentation;

import com.sparta.slackservice.application.SlackMessageService;
import com.sparta.slackservice.domain.SlackMessageSearchCondition;
import com.sparta.slackservice.global.response.RestApiResponse;
import com.sparta.slackservice.infrastructure.client.slack.TemporarySlackClient;
import com.sparta.slackservice.presentation.request.SlackMessageCreateRequest;
import com.sparta.slackservice.presentation.request.SlackMessageUpdateRequest;
import com.sparta.slackservice.presentation.response.SlackMessageCreateResponse;
import com.sparta.slackservice.presentation.response.SlackMessageDetailResponse;
import com.sparta.slackservice.presentation.response.SlackMessagePageResponse;
import com.sparta.slackservice.presentation.response.SlackMessageUpdateResponse;
import jakarta.validation.Valid;
import jakarta.ws.rs.GET;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/slack-messages")
public class SlackMessageController {

    private final SlackMessageService slackMessageService;

    //* Slack 메시지 발송
    /*
     * 요청받은 주문, 허브, 수신자, 메시지 정보를 서비스 계층으로 전달한다.
     * 현재는 TemporarySlackClient를 사용해 실제 Slack API 호출 없이 로컬에서 발송 성공 흐름을 검증한다.
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RestApiResponse<SlackMessageCreateResponse> createSlackMessage(
            @Valid @RequestBody SlackMessageCreateRequest request
    ) {
        // TODO: Gateway 인증·인가 전달 방식이 확정되면 권한을 검증한다.
         /* 발송 가능 대상:
         * - 로그인 사용자
         * - 내부 시스템
         */
        SlackMessageCreateResponse response = slackMessageService.createSlackMessage(request);

        return RestApiResponse.success(
                HttpStatus.CREATED,
                "Slack 메시지 발송 요청이 처리되었습니다.",
                response
        );
    }

    // Slack 메시지 단건 조회
    // slackUserId, channelId, slackTs, failureReason 같은 상세 연동 정보는 단건 조회 응답에서만 확인한다.
    @GetMapping("/{slackMessageId}")
    public RestApiResponse<SlackMessageDetailResponse> getSlackMessage(
            @PathVariable UUID slackMessageId
    ) {
        // TODO: Gateway 권한 전달 방식 확정 후 MASTER 권한 검증

        SlackMessageDetailResponse response =
                slackMessageService.getSlackMessage(slackMessageId);

        return RestApiResponse.success(
                HttpStatus.OK,
                "Slack 메시지 조회에 성공했습니다.",
                response
        );
    }

    // Slack 메시지 목록 및 검색
    // SlackMessageSearchCondition은 쿼리 파라미터로 바인딩된다.
    @GetMapping
    public RestApiResponse<SlackMessagePageResponse> searchSlackMessages(
            @ModelAttribute SlackMessageSearchCondition condition,
            @PageableDefault(page = 0, size = 10) Pageable pageable
    ) {
        // TODO: Gateway 권한 전달 방식 확정 후 MASTER 권한 검증

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
     /* 수정 가능한 값은 message 하나뿐이다. SENT 또는 MODIFIED 상태의 메시지만 수정할 수 있다.
     *
     * TemporarySlackClient 단계에서는 실제 Slack 메시지 대신
     * 임시 수정 성공 결과를 반환하고, 성공 후 DB 내용을 변경한다.
     */
    @PatchMapping("/{slackMessageId}")
    public RestApiResponse<SlackMessageUpdateResponse> updateSlackMessage(
            @PathVariable UUID slackMessageId,
            @Valid @RequestBody SlackMessageUpdateRequest request
    ) {
        // TODO: Gateway 권한 전달 방식 확정 후 MASTER 권한 검증

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
    /*
     * DB의 deletedAt, deletedBy만 기록하는 논리 삭제다.
     * Slack에 이미 발송된 실제 메시지는 삭제하지 않는다.
     */
    @DeleteMapping("/{slackMessageId}")
    public RestApiResponse<Void> deleteSlackMessage(
            @PathVariable UUID slackMessageId
    ) {
        /*
         * TODO: Gateway 인증 정보에서 실제 요청 사용자 ID를 추출한다.
         * TODO: MASTER 권한인지 검증한다.
         *
         * 현재는 로컬 CRUD 검증을 위해 임시 삭제자 UUID를 사용한다.
         */
        UUID temporaryDeletedBy =
                UUID.fromString(
                        "00000000-0000-0000-0000-000000000001"
                );

        slackMessageService.deleteSlackMessage(
                slackMessageId,
                temporaryDeletedBy
        );

        return RestApiResponse.success(
                HttpStatus.OK,
                "Slack 메시지 발송 이력이 삭제되었습니다.",
                null
        );
    }
}
