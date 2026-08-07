package com.sparta.slackservice.infrastructure.client.slack;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@DisplayName("Slack API Client 테스트")
public class SlackApiClientTest {
    // 실제 Slack 서버를 호출하지 않고 SlackApiClient의 요청 형식, 응답 매핑, Slack 오류 및 HTTP 통신 오류 변환을 검증

    private static final String BASE_URL = "https://slack.com/api";
    private static final String BOT_TOKEN = "xoxb-test-token";
    private static final String SLACK_USER_ID = "U1234567890";
    private static final String CHANNEL_ID = "D1234567890";
    private static final String SLACK_TS = "1754400000.123456";

    private MockRestServiceServer mockServer;
    private SlackApiClient slackApiClient;

    @BeforeEach
    void setUp() {
        /*
         * 실제 Slack 서버 대신 요청을 가로채는 가짜 HTTP 서버를 만든다.
         *
         * SlackApiClient는 실제 RestClient를 사용하지만,
         * 네트워크 요청은 MockRestServiceServer가 받아 미리 지정한 응답을 반환한다.
         */
        RestClient.Builder restClientBuilder = RestClient.builder()
                .baseUrl(BASE_URL)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + BOT_TOKEN)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);

        mockServer = MockRestServiceServer.bindTo(restClientBuilder).build();
        slackApiClient = new SlackApiClient(restClientBuilder.build());
    }

    @Nested
    @DisplayName("Slack 메시지 발송")
    class SendDirectMessage {

        @Test
        @DisplayName("chat.postMessage 요청이 성공하면 Slack 발송 결과를 반환한다")
        void sendDirectMessage_success() {
            // given
            String message = "Slack 메시지 발송 테스트입니다.";

            // SlackApiClient가 보내야 하는 요청의 URL, HTTP Method, 인증 헤더와 JSON Body를 미리 정의한다.
            mockServer.expect(once(), requestTo(BASE_URL + "/chat.postMessage"))
                    .andExpect(method(HttpMethod.POST))
                    .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer " + BOT_TOKEN))
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(content().json("""
                        {
                          "channel": "U1234567890",
                          "text": "Slack 메시지 발송 테스트입니다."
                        }
                        """))
                    .andRespond(withSuccess("""
                        {
                          "ok": true,
                          "channel": "D1234567890",
                          "ts": "1754400000.123456"
                        }
                        """, MediaType.APPLICATION_JSON));

            // when
            SlackSendResult result = slackApiClient.sendDirectMessage(SLACK_USER_ID, message);

            // then
            assertThat(result.channelId()).isEqualTo(CHANNEL_ID);
            assertThat(result.slackTs()).isEqualTo(SLACK_TS);
            assertThat(result.sentAt()).isNotNull();

            // 정의한 Slack API 요청이 정확히 한 번 실행됐는지 확인한다.
            mockServer.verify();
        }

        @Test
        @DisplayName("Slack이 ok=false를 반환하면 SlackApiException으로 변환한다")
        void sendDirectMessage_slackApiFailure() {
            // given
            mockServer.expect(once(), requestTo(BASE_URL + "/chat.postMessage"))
                    .andExpect(method(HttpMethod.POST))
                    .andRespond(withSuccess("""
                        {
                          "ok": false,
                          "error": "channel_not_found"
                        }
                        """, MediaType.APPLICATION_JSON));

            // when & then
            assertThatThrownBy(() -> slackApiClient.sendDirectMessage(SLACK_USER_ID, "발송 실패 테스트"))
                    .isInstanceOfSatisfying(SlackApiException.class, exception -> {
                        assertThat(exception.getMessage()).isEqualTo("Slack 메시지 발송에 실패했습니다.");
                        assertThat(exception.getSlackError()).isEqualTo("channel_not_found");
                    });

            mockServer.verify();
        }

        @Test
        @DisplayName("발송 성공 응답에 channel 또는 ts가 없으면 예외가 발생한다")
        void sendDirectMessage_missingIdentifier() {
            // given
            mockServer.expect(once(), requestTo(BASE_URL + "/chat.postMessage"))
                    .andExpect(method(HttpMethod.POST))
                    .andRespond(withSuccess("""
                        {
                          "ok": true
                        }
                        """, MediaType.APPLICATION_JSON));

            // when & then
            assertThatThrownBy(() -> slackApiClient.sendDirectMessage(SLACK_USER_ID, "식별 정보 누락 테스트"))
                    .isInstanceOfSatisfying(SlackApiException.class, exception -> {
                        assertThat(exception.getMessage()).isEqualTo("Slack 메시지 식별 정보가 없습니다.");
                        assertThat(exception.getSlackError()).isEqualTo("missing_message_identifier");
                    });

            mockServer.verify();
        }

        @Test
        @DisplayName("Slack 서버가 HTTP 오류를 반환하면 통신 오류 예외로 변환한다")
        void sendDirectMessage_communicationFailure() {
            // given
            mockServer.expect(once(), requestTo(BASE_URL + "/chat.postMessage"))
                    .andExpect(method(HttpMethod.POST))
                    .andRespond(withServerError());

            // RestClient에서 발생한 HTTP 예외가 그대로 노출되지 않고SlackApiException의 communication_error로 변환되는지 확인한다.
            // when & then
            assertThatThrownBy(() -> slackApiClient.sendDirectMessage(SLACK_USER_ID, "통신 오류 테스트"))
                    .isInstanceOfSatisfying(SlackApiException.class, exception -> {
                        assertThat(exception.getMessage()).isEqualTo("Slack 메시지 발송 중 통신 오류가 발생했습니다.");
                        assertThat(exception.getSlackError()).isEqualTo("communication_error");
                        assertThat(exception.getCause()).isNotNull();
                    });

            mockServer.verify();
        }
    }

    @Nested
    @DisplayName("Slack 메시지 수정")
    class UpdateMessage {

        @Test
        @DisplayName("chat.update 요청이 성공하면 수정된 Slack 메시지 식별자를 반환한다")
        void updateMessage_success() {
            // given
            String updatedMessage = "수정된 Slack 메시지입니다.";

            // chat.update 요청에는 수정 대상의 channel, ts와변경할 새로운 text가 모두 포함되어야 한다.
            mockServer.expect(once(), requestTo(BASE_URL + "/chat.update"))
                    .andExpect(method(HttpMethod.POST))
                    .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer " + BOT_TOKEN))
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(content().json("""
                        {
                          "channel": "D1234567890",
                          "ts": "1754400000.123456",
                          "text": "수정된 Slack 메시지입니다."
                        }
                        """))
                    .andRespond(withSuccess("""
                        {
                          "ok": true,
                          "channel": "D1234567890",
                          "ts": "1754400000.123456"
                        }
                        """, MediaType.APPLICATION_JSON));

            // when
            SlackUpdateResult result = slackApiClient.updateMessage(CHANNEL_ID, SLACK_TS, updatedMessage);

            // then
            assertThat(result.channelId()).isEqualTo(CHANNEL_ID);
            assertThat(result.slackTs()).isEqualTo(SLACK_TS);

            mockServer.verify();
        }

        @Test
        @DisplayName("Slack 메시지 수정이 거부되면 SlackApiException으로 변환한다")
        void updateMessage_slackApiFailure() {
            // given
            mockServer.expect(once(), requestTo(BASE_URL + "/chat.update"))
                    .andExpect(method(HttpMethod.POST))
                    .andRespond(withSuccess("""
                        {
                          "ok": false,
                          "error": "cant_update_message"
                        }
                        """, MediaType.APPLICATION_JSON));

            // when & then
            assertThatThrownBy(() -> slackApiClient.updateMessage(CHANNEL_ID, SLACK_TS, "수정 실패 테스트"))
                    .isInstanceOfSatisfying(SlackApiException.class, exception -> {
                        assertThat(exception.getMessage()).isEqualTo("Slack 메시지 수정에 실패했습니다.");
                        assertThat(exception.getSlackError()).isEqualTo("cant_update_message");
                    });

            mockServer.verify();
        }
    }
}

