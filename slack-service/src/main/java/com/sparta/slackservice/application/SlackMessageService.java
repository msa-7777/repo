package com.sparta.slackservice.application;

import com.sparta.slackservice.domain.SlackFailureReason;
import com.sparta.slackservice.domain.SlackMessage;
import com.sparta.slackservice.domain.SlackMessageRepository;
import com.sparta.slackservice.domain.SlackMessageSearchCondition;
import com.sparta.slackservice.global.exception.ApiException;
import com.sparta.slackservice.global.exception.SlackMessageErrorCode;
import com.sparta.slackservice.infrastructure.client.slack.SlackClient;
import com.sparta.slackservice.infrastructure.client.slack.SlackSendResult;
import com.sparta.slackservice.infrastructure.client.user.UserApiResponse;
import com.sparta.slackservice.infrastructure.client.user.UserClient;
import com.sparta.slackservice.infrastructure.client.user.UserResponse;
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
import org.springframework.beans.factory.annotation.Value;
import com.sparta.slackservice.infrastructure.client.slack.SlackApiException;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SlackMessageService {

    private final SlackMessageRepository slackMessageRepository;

    // 실제 Slack Web API 호출
    private final SlackClient slackClient;

    // receiverId로 사용자 이름과 실제 Slack 사용자 ID를 조회한다.
    private final UserClient userClient;

    /*
     * Slack 메시지 발송 흐름
     *
     * 1. 주문/배송 처리 흐름에서 SlackMessageCreateRequest를 전달받는다.
     * 2. receiverId로 user-service 내부 API를 호출한다.
     * 3. receiverName과 slackId를 조회한다.
     * 4. Slack API를 호출한다.
     * 5. 발송 성공 시 SENT, 실패 시 FAILED 상태로 발송 이력을 저장한다.
     *
     * SlackMessageCreateRequest 값의 출처:
     * - orderId    : Delivery.orderId
     * - hubId      : DeliveryRouteRecord.startHubId
     * - receiverId : DeliveryRouteRecord.deliveryManagerId (= User.userId)
     * - message    : AI 서비스가 생성한 메시지를 주문/배송 처리 흐름에서 전달
     *
     * Slack 서비스는 Delivery 또는 AI 서비스를 직접 호출하지 않는다.
     */
    @Transactional
    public SlackMessageCreateResponse createSlackMessage(
            SlackMessageCreateRequest request
    ) {
        /*
         * 같은 orderId와 receiverId에 대한 중복 발송도 허용하므로
         * 중복 검사 없이 요청마다 새로운 SlackMessage를 생성한다.
         */
        SlackMessage slackMessage = SlackMessage.create(
                request.orderId(),
                request.hubId(),
                request.receiverId(),
                request.message()
        );

        // receiverId는 배송 담당자의 userId와 동일하다.
        ReceiverInfo receiverInfo = getReceiverInfo(request.receiverId());

        slackMessage.assignReceiver(
                receiverInfo.receiverName(),
                receiverInfo.slackUserId()
        );

        try {
            /*
             * 정책:
             * - Slack API 호출은 한 번만 수행
             * - 자동 재시도 없음
             */
            SlackSendResult sendResult = slackClient.sendDirectMessage(
                            receiverInfo.slackUserId(),
                            request.message()
                    );

            slackMessage.markAsSent(sendResult.channelId(),
                                    sendResult.slackTs(),
                                    sendResult.sentAt()
            );
        } catch (SlackApiException exception) {
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
                    "Slack 메시지 발송 실패. receiverId={}, slackError={}, failureReason={}",
                    request.receiverId(),
                    exception.getSlackError(),
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

        // 단건 조회 응답에서는 운영 확인을 위해 slackUserId, channelId, slackTs, failureReason을 포함한다.
        return SlackMessageDetailResponse.from(slackMessage);
    }


    // Slack 메시지 발송 이력을 조건별로 검색한다.
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
            slackClient.updateMessage(
                    slackMessage.getChannelId(),
                    slackMessage.getSlackTs(),
                    request.message()
            );
        } catch (Exception exception) {
            // Slack 수정에 실패하면 DB의 message와 status를 변경하지 않는다.
            log.error(
                    "Slack 메시지 수정 실패. slackMessageId={}",
                    slackMessageId,
                    exception
            );

            throw new ApiException(
                    SlackMessageErrorCode.SLACK_MESSAGE_UPDATE_FAILED
            );
        }

        // Slack 수정이 성공한 경우에만 DB 내용을 변경한다.
        /*
         * - message 변경
         * - status -> MODIFIED
         * - failureReason -> NULL
         */
        slackMessage.updateMessage(request.message());

        SlackMessage updatedSlackMessage =
                slackMessageRepository.saveAndFlush(slackMessage);

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


    /* receiverId로 user-service 내부 API를 호출해  Slack 발송에 필요한 사용자 정보를 조회한다.
     *
     * GET /api/v1/internal/users/{userId}
     */
    private ReceiverInfo getReceiverInfo(UUID receiverId) {

        UserApiResponse<UserResponse> response = userClient.getUser(receiverId);

        if (response == null || !response.success()) {
            throw new ApiException(
                    SlackMessageErrorCode.SLACK_MESSAGE_RECEIVER_NOT_FOUND
            );
        }

        UserResponse user = response.data();

        validateUserResponse(receiverId, user);

        return new ReceiverInfo(
                user.name(),
                user.slackId()
        );
    }

    // user-service 응답에서 Slack 발송에 필요한 값이 정상인지 검증한다.
    private void validateUserResponse(
            UUID receiverId,
            UserResponse user
    ) {

        if (user == null
                || user.userId() == null
                || !receiverId.equals(user.userId())) {

            throw new ApiException(
                    SlackMessageErrorCode.SLACK_MESSAGE_RECEIVER_NOT_FOUND
            );
        }

        if (user.name() == null || user.name().isBlank()) {
            throw new ApiException(
                    SlackMessageErrorCode.SLACK_MESSAGE_RECEIVER_NOT_FOUND
            );
        }

        if (user.slackId() == null || user.slackId().isBlank()) {
            throw new ApiException(
                    SlackMessageErrorCode.SLACK_MESSAGE_RECEIVER_SLACK_ID_NOT_FOUND
            );
        }
    }


    // Slack 발송 예외를 DB에 저장할 실패 사유로 변환한다.
    private SlackFailureReason resolveSendFailureReason(
            SlackApiException exception
    ) {
        return switch (exception.getSlackError()) {
            case "channel_not_found" -> SlackFailureReason.CHANNEL_CREATE_FAILED;
            case "communication_error" -> SlackFailureReason.API_TIMEOUT;
            default -> SlackFailureReason.MESSAGE_SEND_FAILED;
        };
    }

    // user-service에서 조회한 사용자 정보를 Slack 발송에 필요한 값으로 한정하여 사용한다.
    private record ReceiverInfo(
            String receiverName,
            String slackUserId
    ) {
    }
}