package com.sparta.slackservice.application;

import com.sparta.slackservice.domain.SlackFailureReason;
import com.sparta.slackservice.domain.SlackMessage;
import com.sparta.slackservice.domain.SlackMessageRepository;
import com.sparta.slackservice.domain.SlackMessageSearchCondition;
import com.sparta.slackservice.global.exception.ApiException;
import com.sparta.slackservice.global.exception.SlackMessageErrorCode;
import com.sparta.slackservice.infrastructure.client.slack.SlackClient;
import com.sparta.slackservice.infrastructure.client.slack.SlackSendResult;
import com.sparta.slackservice.presentation.request.SlackMessageCreateRequest;
import com.sparta.slackservice.presentation.request.SlackMessageUpdateRequest;
import com.sparta.slackservice.presentation.response.SlackMessageCreateResponse;
import com.sparta.slackservice.presentation.response.SlackMessageDetailResponse;
import com.sparta.slackservice.presentation.response.SlackMessagePageResponse;
import com.sparta.slackservice.presentation.response.SlackMessageSummaryResponse;
import com.sparta.slackservice.presentation.response.SlackMessageUpdateResponse;
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
public class SlackMessageService {

    private final SlackMessageRepository slackMessageRepository;

    /* 현재 로컬 환경에서는 TemporarySlackClient가 주입된다.
     *
     * 서비스가 TemporarySlackClient 구현체에 직접 의존하지 않고
     * SlackClient 인터페이스에 의존하도록 구성하면,
     * 이후 실제 Slack API 구현체로 쉽게 교체할 수 있다.
     */
    private final SlackClient slackClient;

    /* Slack 메시지를 발송하고 발송 결과를 저장한다.
     *
     * 현재 로컬 개발 흐름:
     * 1. 발송 요청으로 SlackMessage 엔티티 생성
     * 2. receiverId를 기반으로 임시 사용자 정보 생성
     * 3. TemporarySlackClient를 통해 임시 Slack 발송
     * 4. 성공하면 SENT, 실패하면 FAILED로 상태 저장
     */
    @Transactional
    public SlackMessageCreateResponse createSlackMessage(
            SlackMessageCreateRequest request
    ) {
        /*
         * 발송 요청에 포함된 업무 정보로 엔티티를 생성한다.
         *
         * 같은 orderId와 receiverId에 대한 중복 발송도 허용하므로
         * 중복 검사 없이 요청마다 새로운 SlackMessage를 생성한다.
         */
        SlackMessage slackMessage = SlackMessage.create(
                request.orderId(),
                request.hubId(),
                request.receiverId(),
                request.message()
        );

        // TODO: user-service 연동
        /*
         * 실제 MSA 연동 시에는 receiverId로 아래 API를 호출한다.
         *
         * GET /api/v1/admin/users/{userId}
         *
         * 공통 응답의 data에서 다음 값을 사용한다.
         * - name -> receiverName
         * - slackId  -> slackUserId
         *
         * 현재는 로컬 CRUD 검증을 위해 임시 사용자 정보를 사용한다.
         */
        TemporaryReceiverInfo receiverInfo = createTemporaryReceiverInfo(request.receiverId());

        slackMessage.assignReceiver(
                receiverInfo.receiverName(),
                receiverInfo.slackUserId()
        );

        try {
            /*
             * 현재는 TemporarySlackClient가 실제 Slack API 대신
             * 임시 channelId, slackTs, sentAt을 반환한다.
             *
             * 정책:
             * - Slack API 호출은 한 번만 수행
             * - 자동 재시도 없음
             */
            SlackSendResult sendResult = slackClient.sendDirectMessage(
                            receiverInfo.slackUserId(),
                            request.message()
                    );

            // 임시 Slack 발송에 성공하면 다음 값들을 저장한다.
            /*
             * - status        : SENT
             * - channelId     : 메시지가 발송된 DM 채널
             * - slackTs       : Slack 메시지 식별값
             * - sentAt        : 발송 시각
             * - failureReason : NULL
             */
            slackMessage.markAsSent(sendResult.channelId(),
                                    sendResult.slackTs(),
                                    sendResult.sentAt());
        } catch (Exception exception) {
            // 발송에 실패해도 발송 요청 이력은 삭제하지 않는다.
            /*
             * - status        : FAILED
             * - failureReason : 실패 원인
             * - channelId     : NULL
             * - slackTs       : NULL
             * - sentAt        : NULL
             */
            SlackFailureReason failureReason = resolveSendFailureReason(exception);

            slackMessage.markAsFailed(failureReason);

            // 메시지 본문이나 Slack Token과 같은 민감 정보는 로그에 출력하지 않는다.
            log.error(
                    "Slack 메시지 발송 실패. receiverId={}, failureReason={}",
                    request.receiverId(),
                    failureReason,
                    exception
            );
        }

        // 발송 성공 또는 실패 결과가 반영된 엔티티를 저장한다.
        /*
         * saveAndFlush를 사용하면 생성일시 등 JPA Auditing 결과를
         * 응답 DTO로 변환하기 전에 DB에 반영할 수 있다.
         */
        SlackMessage savedSlackMessage = slackMessageRepository.saveAndFlush(slackMessage);

        // TODO: 외부 API와 DB 사이의 상태 불일치 보완
        /* 실제 Slack 발송은 성공했지만 DB 저장이 실패하면
         * Slack에는 메시지가 존재하고 DB에는 이력이 없는 상황이 발생할 수 있다.
         *
         * 현재는 예외 로그만 남기고, 추후 Outbox 패턴,
         * 이벤트 기반 처리 또는 보상 처리 도입을 검토한다.
         */

        return SlackMessageCreateResponse.from(savedSlackMessage);
    }


    // 삭제되지 않은 Slack 메시지 발송 이력을 단건 조회한다.
    public SlackMessageDetailResponse getSlackMessage(
            UUID slackMessageId
    ) {
        SlackMessage slackMessage =
                findSlackMessage(slackMessageId);

        /* 단건 조회 응답에서는 운영 확인을 위해
         * slackUserId, channelId, slackTs, failureReason을 포함한다.
         */
        return SlackMessageDetailResponse.from(slackMessage);
    }


    // Slack 메시지 발송 이력을 조건별로 검색한다.
    /* 검색 조건:
     * - orderId
     * - hubId
     * - receiverId
     * - receiverName
     * - status
     * - startDate, endDate(createdAt 기준)
     *
     * 정렬:
     * - createdAt DESC
     *
     * 공통 조건:
     * - deletedAt IS NULL
     */
    public SlackMessagePageResponse searchSlackMessages(
            SlackMessageSearchCondition condition,
            Pageable pageable
    ) {
        Page<SlackMessageSummaryResponse> result = slackMessageRepository
                        .search(condition, pageable)
                        .map(SlackMessageSummaryResponse::from);

        // 목록 응답에는 내부 Slack 연동 정보인 slackUserId, channelId, slackTs를 포함하지 않는다.
        return SlackMessagePageResponse.from(result);
    }


    // 이미 발송된 Slack 메시지의 내용만 수정한다.
    /*
     * 수정 가능 상태:
     * - SENT
     * - MODIFIED
     *
     * 수정 불가능 상태:
     * - FAILED
     */
    @Transactional
    public SlackMessageUpdateResponse updateSlackMessage(
            UUID slackMessageId,
            SlackMessageUpdateRequest request
    ) {
        SlackMessage slackMessage = findSlackMessage(slackMessageId);

        // FAILED 상태는 실제 Slack 메시지가 존재하지 않으므로 수정할 수 없다.
        if (!slackMessage.isModifiable()) {
            throw new ApiException(
                    SlackMessageErrorCode.SLACK_MESSAGE_NOT_MODIFIABLE
            );
        }

        // Slack 메시지 수정에는 최초 발송 시 저장한 channelId와 slackTs가 필요하다.
        if (slackMessage.getChannelId() == null
                || slackMessage.getSlackTs() == null) {
            throw new ApiException(
                    SlackMessageErrorCode
                            .SLACK_MESSAGE_IDENTIFIER_NOT_FOUND
            );
        }

        try {
            /*
             * 현재는 TemporarySlackClient가 수정 성공 결과를 반환한다.
             *
             * 실제 연동 시에는 Slack chat.update API를 호출하도록
             * SlackClient 구현체를 교체한다.
             */
            slackClient.updateMessage(
                    slackMessage.getChannelId(),
                    slackMessage.getSlackTs(),
                    request.message()
            );
        } catch (Exception exception) {
            /*
             * Slack 수정에 실패하면 DB의 message와 status를
             * 변경하지 않는다.
             *
             * 기존 상태가 SENT이면 SENT 유지,
             * 기존 상태가 MODIFIED이면 MODIFIED 유지한다.
             */
            log.error(
                    "Slack 메시지 수정 실패. slackMessageId={}",
                    slackMessageId,
                    exception
            );

            throw new ApiException(
                    SlackMessageErrorCode.SLACK_MESSAGE_UPDATE_FAILED
            );
        }

        /*
         * Slack 수정이 성공한 경우에만 DB 내용을 변경한다.
         *
         * - message 변경
         * - status -> MODIFIED
         * - failureReason -> NULL
         */
        slackMessage.updateMessage(request.message());

        SlackMessage updatedSlackMessage =
                slackMessageRepository.saveAndFlush(slackMessage);

        /*
         * TODO: Slack 수정 성공 후 DB 저장 실패 보완
         *
         * Slack에서는 수정됐지만 DB 수정이 실패하는 불일치 상황에 대해
         * 추후 보상 요청 또는 이벤트 기반 처리 도입을 검토한다.
         */

        return SlackMessageUpdateResponse.from(updatedSlackMessage);
    }


    // Slack 메시지 발송 이력을 논리 삭제한다.
    // 실제 Slack에 발송된 메시지는 삭제하지 않는다.
    @Transactional
    public void deleteSlackMessage(
            UUID slackMessageId,
            UUID deletedBy
    ) {
        SlackMessage slackMessage = findSlackMessage(slackMessageId);

        /*
         * DELETE API의 의미:
         * - DB 발송 이력만 논리 삭제
         * - Slack에 발송된 실제 메시지는 유지
         *
         * TODO: Gateway 인증 방식 확정 후 deletedBy에 실제 요청 사용자 ID를 전달한다.
         */
        slackMessage.delete(deletedBy);
    }

    // 삭제되지 않은 Slack 메시지를 조회한다.
    // 존재하지 않거나 이미 논리 삭제된 메시지는 동일하게 NOT_FOUND로 처리한다.
    private SlackMessage findSlackMessage(UUID slackMessageId) {
        return slackMessageRepository
                .findByIdAndDeletedAtIsNull(slackMessageId)
                .orElseThrow(() -> new ApiException(
                        SlackMessageErrorCode.SLACK_MESSAGE_NOT_FOUND
                ));
    }


    // 로컬 CRUD 검증을 위한 임시 수신자 정보를 생성한다.
    // receiverId는 요청값을 그대로 사용하고, receiverName과 slackUserId만 임시값으로 만든다.
    private TemporaryReceiverInfo createTemporaryReceiverInfo(
            UUID receiverId
    ) {
        /*
         * TODO: user-service FeignClient 연동
         *
         * 추후 아래 임시값 생성 로직을 제거하고
         * user-service의 공통 응답 data를 사용한다.
         */
        String receiverName = "임시 담당자-" + receiverId.toString().substring(0, 8);

        String slackUserId =
                "U_TEMP_" + receiverId
                        .toString()
                        .replace("-", "")
                        .substring(0, 10)
                        .toUpperCase();

        return new TemporaryReceiverInfo(receiverName, slackUserId);
    }

    /**
     * Slack 발송 예외를 DB에 저장할 실패 사유로 변환한다.
     *
     * 현재 TemporarySlackClient 단계에서는 상세한 외부 API
     * 예외 유형이 없으므로 기본적으로 MESSAGE_SEND_FAILED를 사용한다.
     */
    private SlackFailureReason resolveSendFailureReason(
            Exception exception
    ) {
        /*
         * TODO: 실제 Slack API 연동 후 예외 유형별로 분리
         *
         * 예:
         * - 사용자 조회 실패       -> USER_NOT_FOUND
         * - Slack ID 없음        -> SLACK_ID_NOT_FOUND
         * - DM 채널 생성 실패      -> CHANNEL_CREATE_FAILED
         * - 요청 시간 초과         -> API_TIMEOUT
         * - 메시지 전송 실패       -> MESSAGE_SEND_FAILED
         */
        return SlackFailureReason.MESSAGE_SEND_FAILED;
    }

    /**
     * user-service 연동 전까지만 사용하는 내부 임시 데이터 구조다.
     */
    private record TemporaryReceiverInfo(
            String receiverName,
            String slackUserId
    ) {
    }
}