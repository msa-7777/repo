package com.sparta.slackservice.infrastructure.client.slack;

import com.sparta.slackservice.infrastructure.client.slack.request.SlackPostMessageRequest;
import com.sparta.slackservice.infrastructure.client.slack.request.SlackUpdateMessageRequest;
import com.sparta.slackservice.infrastructure.client.slack.response.SlackMessageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.time.LocalDateTime;

@Component
/*@Profile({"local", "dev"})*/
@RequiredArgsConstructor
public class SlackApiClient implements SlackClient {

    private final RestClient slackRestClient;

    /**
     * Slack 사용자에게 실제 DM을 발송한다.
     *
     * 처리 흐름:
     * 1. slackUserId와 message로 chat.postMessage 요청 생성
     * 2. Slack Web API 호출
     * 3. 응답 본문의 ok 값 검증
     * 4. 실제 DM channel과 메시지 ts를 내부 결과로 반환
     */
    @Override
    public SlackSendResult sendDirectMessage(String slackUserId, String message) {

        try {
            SlackPostMessageRequest request = new SlackPostMessageRequest(slackUserId, message);

            SlackMessageResponse response = slackRestClient.post()
                    .uri("/chat.postMessage")       // chat.postMessage는 채널이나 DM에 메시지를 게시
                    .body(request)
                    .retrieve()
                    .body(SlackMessageResponse.class);

            validatePostMessageResponse(response);

            return new SlackSendResult(response.channel(), response.ts(), LocalDateTime.now());

        } catch (SlackApiException exception) {
            throw exception;
        } catch (RestClientException exception) {
            throw new SlackApiException("Slack 메시지 발송 중 통신 오류가 발생했습니다.", "communication_error", exception);
        }

    }

    /**
     * 기존에 발송된 Slack 메시지 내용을 수정한다.
     *
     * chat.update에는 다음 값이 필요하다.
     * - channelId: 메시지가 존재하는 Slack DM 채널 ID
     * - slackTs: 수정 대상 메시지의 Slack timestamp
     * - message: 새 메시지 내용
     */
    @Override
    public SlackUpdateResult updateMessage(String channelId, String slackTs, String message) {

        try {
            SlackUpdateMessageRequest request = new SlackUpdateMessageRequest(channelId, slackTs, message);

            SlackMessageResponse response = slackRestClient.post()
                    .uri("/chat.update")            // chat.update는 기존 채널 메시지를 수정
                    .body(request)
                    .retrieve()
                    .body(SlackMessageResponse.class);

            validateUpdateMessageResponse(response);

            return new SlackUpdateResult(response.channel(), response.ts());
        } catch (SlackApiException exception) {
            throw exception;
        } catch (RestClientException exception) {
            throw new SlackApiException("Slack 메시지 수정 중 통신 오류가 발생했습니다.", "communication_error", exception);
        }

    }

    /**
     * HTTP 호출 자체가 성공해도 Slack의 응답 본문에서 ok=false가 올 수 있다.
     *
     * 따라서 응답이 없거나 ok=false인 경우에는 발송 실패 예외로 처리한다.
     */
    private void validatePostMessageResponse(SlackMessageResponse response) {
        if (response == null) {
            throw new SlackApiException("Slack 메시지 발송 응답이 없습니다.", "empty_response");
        }

        if (!response.ok()) {
            throw new SlackApiException("Slack 메시지 발송에 실패했습니다.", response.error());
        }

        if (response.channel() == null || response.ts() == null) {
            throw new SlackApiException("Slack 메시지 식별 정보가 없습니다.", "missing_message_identifier");
        }
    }

    /**
     * 수정 API도 HTTP 상태뿐 아니라 Slack 응답의 ok 값을 확인한다.
     */
    private void validateUpdateMessageResponse(SlackMessageResponse response) {
        if (response == null) {
            throw new SlackApiException("Slack 메시지 수정 응답이 없습니다.", "empty_response");
        }

        if (!response.ok()) {
            throw new SlackApiException("Slack 메시지 수정에 실패했습니다.", response.error());
        }

        if (response.channel() == null || response.ts() == null) {
            throw new SlackApiException("수정된 Slack 메시지 식별 정보가 없습니다.", "missing_message_identifier");
        }
    }
}