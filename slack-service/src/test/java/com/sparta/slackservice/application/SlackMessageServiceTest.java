package com.sparta.slackservice.application;

import com.sparta.slackservice.domain.SlackFailureReason;
import com.sparta.slackservice.domain.SlackMessage;
import com.sparta.slackservice.domain.SlackMessageRepository;
import com.sparta.slackservice.domain.SlackMessageStatus;
import com.sparta.slackservice.global.exception.ApiException;
import com.sparta.slackservice.global.exception.SlackMessageErrorCode;
import com.sparta.slackservice.infrastructure.client.slack.SlackApiException;
import com.sparta.slackservice.infrastructure.client.slack.SlackClient;
import com.sparta.slackservice.infrastructure.client.slack.SlackSendResult;
import com.sparta.slackservice.infrastructure.client.slack.SlackUpdateResult;
import com.sparta.slackservice.presentation.request.SlackMessageCreateRequest;
import com.sparta.slackservice.presentation.request.SlackMessageUpdateRequest;
import com.sparta.slackservice.presentation.response.SlackMessageCreateResponse;
import com.sparta.slackservice.presentation.response.SlackMessageUpdateResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Slack 메시지 서비스 테스트")
class SlackMessageServiceTest {
    // Slack API 결과에 따라 SlackMessageService가 비즈니스 정책(SENT, FAILED, MODIFIED, 실패 사유 저장 등)을 올바르게 수행하는지 검증

    private static final UUID ORDER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID HUB_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final UUID RECEIVER_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");
    private static final UUID SLACK_MESSAGE_ID = UUID.fromString("44444444-4444-4444-4444-444444444444");
    private static final UUID DELETED_BY = UUID.fromString("55555555-5555-5555-5555-555555555555");

    private static final String SLACK_USER_ID = "U1234567890";
    private static final String CHANNEL_ID = "D1234567890";
    private static final String SLACK_TS = "1754400000.123456";
    private static final String ORIGINAL_MESSAGE = "배송을 시작해 주세요.";

    @Mock
    private SlackMessageRepository slackMessageRepository;

    @Mock
    private SlackClient slackClient;

    @InjectMocks
    private SlackMessageService slackMessageService;

    @BeforeEach
    void setUp() {
        // @InjectMocks는 생성자 의존성은 주입하지만, @Value 필드까지 자동으로 주입하지는 않는다.
        // 실제 테스트용 Slack Member ID를 대신해 ReflectionTestUtils로 임시 값을 설정한다.
        ReflectionTestUtils.setField(slackMessageService, "temporarySlackUserId", SLACK_USER_ID);

        // Service가 saveAndFlush()에 전달한 엔티티를 그대로 반환하게 한다.
        // 단위 테스트에서는 실제 DB가 없으므로, Repository가 저장 결과로 같은 엔티티를 반환한다고 가정한다.
        lenient().when(slackMessageRepository.saveAndFlush(any(SlackMessage.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Nested
    @DisplayName("Slack 메시지 발송")
    class CreateSlackMessage {

        @Test
        @DisplayName("Slack 발송에 성공하면 SENT 상태와 Slack 식별 정보를 저장한다")
        void createSlackMessage_success() {
            // given
            SlackMessageCreateRequest request = createRequest(ORIGINAL_MESSAGE);
            LocalDateTime sentAt = LocalDateTime.now();

            when(slackClient.sendDirectMessage(SLACK_USER_ID, ORIGINAL_MESSAGE))
                    .thenReturn(new SlackSendResult(CHANNEL_ID, SLACK_TS, sentAt));

            // when
            SlackMessageCreateResponse response = slackMessageService.createSlackMessage(request);

            // then
            // Repository에 저장된 실제 엔티티를 꺼내발송 성공 결과가 올바르게 반영되었는지 확인한다.
            ArgumentCaptor<SlackMessage> captor = ArgumentCaptor.forClass(SlackMessage.class);
            verify(slackMessageRepository).saveAndFlush(captor.capture());

            SlackMessage savedMessage = captor.getValue();

            assertThat(savedMessage.getOrderId()).isEqualTo(ORDER_ID);
            assertThat(savedMessage.getHubId()).isEqualTo(HUB_ID);
            assertThat(savedMessage.getReceiverId()).isEqualTo(RECEIVER_ID);
            assertThat(savedMessage.getSlackUserId()).isEqualTo(SLACK_USER_ID);
            assertThat(savedMessage.getMessage()).isEqualTo(ORIGINAL_MESSAGE);

            assertThat(savedMessage.getStatus()).isEqualTo(SlackMessageStatus.SENT);
            assertThat(savedMessage.getChannelId()).isEqualTo(CHANNEL_ID);
            assertThat(savedMessage.getSlackTs()).isEqualTo(SLACK_TS);
            assertThat(savedMessage.getSentAt()).isEqualTo(sentAt);
            assertThat(savedMessage.getFailureReason()).isNull();

            assertThat(response.status()).isEqualTo(SlackMessageStatus.SENT);
            assertThat(response.sentAt()).isEqualTo(sentAt);

            // 자동 재시도 정책이 없으므로 Slack 발송 호출은 정확히 한 번이어야 한다.
            verify(slackClient, times(1)).sendDirectMessage(SLACK_USER_ID, ORIGINAL_MESSAGE);
        }

        @Test
        @DisplayName("Slack 채널을 찾지 못하면 FAILED와 CHANNEL_CREATE_FAILED를 저장한다")
        void createSlackMessage_channelNotFound() {
            // given
            SlackMessageCreateRequest request = createRequest(ORIGINAL_MESSAGE);

            when(slackClient.sendDirectMessage(SLACK_USER_ID, ORIGINAL_MESSAGE))
                    .thenThrow(new SlackApiException(
                            "Slack 메시지 발송에 실패했습니다.",
                            "channel_not_found"
                    ));

            // when
            SlackMessageCreateResponse response = slackMessageService.createSlackMessage(request);

            // then
            ArgumentCaptor<SlackMessage> captor = ArgumentCaptor.forClass(SlackMessage.class);
            verify(slackMessageRepository).saveAndFlush(captor.capture());

            SlackMessage savedMessage = captor.getValue();

            // Slack에는 메시지가 생성되지 않았으므로 channelId, slackTs, sentAt은 저장되지 않아야 한다.
            assertThat(savedMessage.getStatus()).isEqualTo(SlackMessageStatus.FAILED);
            assertThat(savedMessage.getFailureReason()).isEqualTo(SlackFailureReason.CHANNEL_CREATE_FAILED.getMessage());
            assertThat(savedMessage.getChannelId()).isNull();
            assertThat(savedMessage.getSlackTs()).isNull();
            assertThat(savedMessage.getSentAt()).isNull();

            assertThat(response.status()).isEqualTo(SlackMessageStatus.FAILED);
            assertThat(response.sentAt()).isNull();

            // 실패해도 재호출하지 않고 실패 이력만 한 번 저장한다.
            verify(slackClient, times(1)).sendDirectMessage(SLACK_USER_ID, ORIGINAL_MESSAGE);
            verify(slackMessageRepository, times(1)).saveAndFlush(any(SlackMessage.class));
        }

        @Test
        @DisplayName("Slack 통신 오류가 발생하면 FAILED와 API_TIMEOUT을 저장한다")
        void createSlackMessage_communicationError() {
            // given
            SlackMessageCreateRequest request = createRequest(ORIGINAL_MESSAGE);

            when(slackClient.sendDirectMessage(SLACK_USER_ID, ORIGINAL_MESSAGE))
                    .thenThrow(new SlackApiException(
                            "Slack 메시지 발송 중 통신 오류가 발생했습니다.",
                            "communication_error"
                    ));

            // when
            slackMessageService.createSlackMessage(request);

            // then
            ArgumentCaptor<SlackMessage> captor = ArgumentCaptor.forClass(SlackMessage.class);
            verify(slackMessageRepository).saveAndFlush(captor.capture());

            SlackMessage savedMessage = captor.getValue();

            assertThat(savedMessage.getStatus()).isEqualTo(SlackMessageStatus.FAILED);
            assertThat(savedMessage.getFailureReason()).isEqualTo(SlackFailureReason.API_TIMEOUT.getMessage());
            assertThat(savedMessage.getChannelId()).isNull();
            assertThat(savedMessage.getSlackTs()).isNull();
            assertThat(savedMessage.getSentAt()).isNull();

            verify(slackClient, times(1)).sendDirectMessage(SLACK_USER_ID, ORIGINAL_MESSAGE);
        }

        @Test
        @DisplayName("분류되지 않은 Slack 오류는 MESSAGE_SEND_FAILED로 저장한다")
        void createSlackMessage_unknownSlackError() {
            // given
            SlackMessageCreateRequest request = createRequest(ORIGINAL_MESSAGE);

            when(slackClient.sendDirectMessage(SLACK_USER_ID, ORIGINAL_MESSAGE))
                    .thenThrow(new SlackApiException(
                            "Slack 인증에 실패했습니다.",
                            "invalid_auth"
                    ));

            // when
            slackMessageService.createSlackMessage(request);

            // then
            ArgumentCaptor<SlackMessage> captor = ArgumentCaptor.forClass(SlackMessage.class);
            verify(slackMessageRepository).saveAndFlush(captor.capture());

            SlackMessage savedMessage = captor.getValue();

            //별도로 매핑하지 않은 Slack 오류는 기본 실패 사유인 MESSAGE_SEND_FAILED로 처리한다.
            assertThat(savedMessage.getStatus()).isEqualTo(SlackMessageStatus.FAILED);
            assertThat(savedMessage.getFailureReason()).isEqualTo(SlackFailureReason.MESSAGE_SEND_FAILED.getMessage());
        }
    }

    @Nested
    @DisplayName("Slack 메시지 수정")
    class UpdateSlackMessage {

        @Test
        @DisplayName("Slack 수정에 성공하면 메시지 내용과 상태를 MODIFIED로 변경한다")
        void updateSlackMessage_success() {
            // given
            SlackMessage slackMessage = createSentSlackMessage();
            SlackMessageUpdateRequest request = new SlackMessageUpdateRequest("수정된 배송 메시지입니다.");

            when(slackMessageRepository.findByIdAndDeletedAtIsNull(SLACK_MESSAGE_ID))
                    .thenReturn(Optional.of(slackMessage));

            when(slackClient.updateMessage(CHANNEL_ID, SLACK_TS, request.message()))
                    .thenReturn(new SlackUpdateResult(CHANNEL_ID, SLACK_TS));

            // when
            SlackMessageUpdateResponse response =
                    slackMessageService.updateSlackMessage(SLACK_MESSAGE_ID, request);

            // then
            assertThat(slackMessage.getMessage()).isEqualTo(request.message());
            assertThat(slackMessage.getStatus()).isEqualTo(SlackMessageStatus.MODIFIED);
            assertThat(slackMessage.getFailureReason()).isNull();

            // Slack 메시지 자체의 식별자는 수정 후에도 동일하게 유지되어야 한다.
            assertThat(slackMessage.getChannelId()).isEqualTo(CHANNEL_ID);
            assertThat(slackMessage.getSlackTs()).isEqualTo(SLACK_TS);

            assertThat(response.message()).isEqualTo(request.message());
            assertThat(response.status()).isEqualTo(SlackMessageStatus.MODIFIED);

            verify(slackClient, times(1)).updateMessage(CHANNEL_ID, SLACK_TS, request.message());
            verify(slackMessageRepository, times(1)).saveAndFlush(slackMessage);
        }

        @Test
        @DisplayName("FAILED 상태의 메시지는 Slack 수정 API를 호출하지 않는다")
        void updateSlackMessage_failedStatus() {
            // given
            SlackMessage failedMessage = createFailedSlackMessage();
            SlackMessageUpdateRequest request = new SlackMessageUpdateRequest("수정하면 안 되는 메시지");

            when(slackMessageRepository.findByIdAndDeletedAtIsNull(SLACK_MESSAGE_ID))
                    .thenReturn(Optional.of(failedMessage));

            // when & then
            assertThatThrownBy(() -> slackMessageService.updateSlackMessage(SLACK_MESSAGE_ID, request))
                    .isInstanceOfSatisfying(ApiException.class, exception ->
                            assertThat(exception.getErrorCode())
                                    .isEqualTo(SlackMessageErrorCode.SLACK_MESSAGE_NOT_MODIFIABLE)
                    );

            // FAILED 상태는 실제 Slack 메시지가 존재하지 않으므로 Slack API와 Repository 저장이 모두 호출되지 않아야 한다.
            verify(slackClient, never()).updateMessage(anyString(), anyString(), anyString());
            verify(slackMessageRepository, never()).saveAndFlush(any(SlackMessage.class));

            assertThat(failedMessage.getMessage()).isEqualTo(ORIGINAL_MESSAGE);
            assertThat(failedMessage.getStatus()).isEqualTo(SlackMessageStatus.FAILED);
        }

        @Test
        @DisplayName("Slack 식별 정보가 없으면 수정 API를 호출하지 않는다")
        void updateSlackMessage_missingIdentifier() {
            // given
            /*
             * 데이터 이상 상황을 표현하기 위해 SlackMessage를 Mock으로 만든다.
             *
             * 상태는 수정 가능하지만 channelId가 없는 비정상 데이터를 가정한다.
             */
            SlackMessage slackMessage = org.mockito.Mockito.mock(SlackMessage.class);
            SlackMessageUpdateRequest request = new SlackMessageUpdateRequest("수정 메시지");

            when(slackMessageRepository.findByIdAndDeletedAtIsNull(SLACK_MESSAGE_ID))
                    .thenReturn(Optional.of(slackMessage));

            when(slackMessage.isModifiable()).thenReturn(true);
            when(slackMessage.getChannelId()).thenReturn(null);

            // when & then
            assertThatThrownBy(() -> slackMessageService.updateSlackMessage(SLACK_MESSAGE_ID, request))
                    .isInstanceOfSatisfying(ApiException.class, exception ->
                            assertThat(exception.getErrorCode())
                                    .isEqualTo(SlackMessageErrorCode.SLACK_MESSAGE_IDENTIFIER_NOT_FOUND)
                    );

            verify(slackClient, never()).updateMessage(anyString(), anyString(), anyString());
            verify(slackMessageRepository, never()).saveAndFlush(any(SlackMessage.class));
        }

        @Test
        @DisplayName("Slack 수정에 실패하면 기존 메시지와 상태를 유지한다")
        void updateSlackMessage_slackApiFailure() {
            // given
            SlackMessage slackMessage = createSentSlackMessage();
            SlackMessageUpdateRequest request = new SlackMessageUpdateRequest("수정에 실패할 메시지");

            when(slackMessageRepository.findByIdAndDeletedAtIsNull(SLACK_MESSAGE_ID))
                    .thenReturn(Optional.of(slackMessage));

            when(slackClient.updateMessage(CHANNEL_ID, SLACK_TS, request.message()))
                    .thenThrow(new SlackApiException(
                            "Slack 메시지 수정에 실패했습니다.",
                            "cant_update_message"
                    ));

            // when & then
            assertThatThrownBy(() -> slackMessageService.updateSlackMessage(SLACK_MESSAGE_ID, request))
                    .isInstanceOfSatisfying(ApiException.class, exception ->
                            assertThat(exception.getErrorCode())
                                    .isEqualTo(SlackMessageErrorCode.SLACK_MESSAGE_UPDATE_FAILED)
                    );

            // Slack 수정이 실패했으므로 DB 엔티티도 변경되면 안 된다.
            assertThat(slackMessage.getMessage()).isEqualTo(ORIGINAL_MESSAGE);
            assertThat(slackMessage.getStatus()).isEqualTo(SlackMessageStatus.SENT);
            assertThat(slackMessage.getChannelId()).isEqualTo(CHANNEL_ID);
            assertThat(slackMessage.getSlackTs()).isEqualTo(SLACK_TS);

            verify(slackClient, times(1)).updateMessage(CHANNEL_ID, SLACK_TS, request.message());
            verify(slackMessageRepository, never()).saveAndFlush(any(SlackMessage.class));
        }
    }

    @Nested
    @DisplayName("Slack 메시지 삭제")
    class DeleteSlackMessage {

        @Test
        @DisplayName("Slack 메시지는 실제 삭제하지 않고 삭제 정보만 기록한다")
        void deleteSlackMessage_success() {
            // given
            SlackMessage slackMessage = createSentSlackMessage();

            when(slackMessageRepository.findByIdAndDeletedAtIsNull(SLACK_MESSAGE_ID))
                    .thenReturn(Optional.of(slackMessage));

            // when
            slackMessageService.deleteSlackMessage(SLACK_MESSAGE_ID, DELETED_BY);

            // then
            // delete()는 Repository의 delete()를 호출하지 않고 엔티티의 deletedAt, deletedBy만 변경하는 논리 삭제
            assertThat(slackMessage.getDeletedAt()).isNotNull();
            assertThat(slackMessage.getDeletedBy()).isEqualTo(DELETED_BY);

            verify(slackMessageRepository, never()).delete(any(SlackMessage.class));
            verify(slackClient, never()).updateMessage(anyString(), anyString(), anyString());
        }
    }

    // 생성 API 테스트에서 반복해서 사용하는 요청 객체를 만든다.
    private SlackMessageCreateRequest createRequest(String message) {
        return new SlackMessageCreateRequest(ORDER_ID, HUB_ID, RECEIVER_ID, message);
    }

    /**
     * Slack 발송이 완료된 SENT 상태의 메시지를 만든다.
     *
     * 수정 테스트에서는 실제 Slack 메시지가 존재해야 하므로 channelId와 slackTs까지 함께 설정한다.
     */
    private SlackMessage createSentSlackMessage() {
        SlackMessage slackMessage = SlackMessage.create(
                ORDER_ID,
                HUB_ID,
                RECEIVER_ID,
                ORIGINAL_MESSAGE
        );

        slackMessage.assignReceiver("임시 담당자", SLACK_USER_ID);
        slackMessage.markAsSent(CHANNEL_ID, SLACK_TS, LocalDateTime.now());

        return slackMessage;
    }

    // Slack 발송에 실패한 FAILED 상태의 메시지를 만든다.
    private SlackMessage createFailedSlackMessage() {
        SlackMessage slackMessage = SlackMessage.create(
                ORDER_ID,
                HUB_ID,
                RECEIVER_ID,
                ORIGINAL_MESSAGE
        );

        slackMessage.assignReceiver("임시 담당자", SLACK_USER_ID);
        slackMessage.markAsFailed(SlackFailureReason.MESSAGE_SEND_FAILED);

        return slackMessage;
    }
}
