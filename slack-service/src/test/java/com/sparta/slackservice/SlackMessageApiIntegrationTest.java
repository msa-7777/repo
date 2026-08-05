package com.sparta.slackservice;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sparta.slackservice.domain.SlackMessage;
import com.sparta.slackservice.domain.SlackMessageRepository;
import com.sparta.slackservice.domain.SlackMessageStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/*
 * Slack 메시지 API 통합 테스트
 *
 * MockMvc를 이용하여 Controller부터 Service, Repository, H2 DB까지 실제 애플리케이션 흐름을 함께 검증한다.
 *
 * 현재 검증 범위:
 * 1. TemporarySlackClient를 사용한 메시지 발송 및 저장
 * 2. Slack 메시지 단건 조회
 * 3. Slack 메시지 목록 및 조건 검색
 * 4. 발송된 Slack 메시지 수정
 * 5. Slack 메시지 발송 이력 논리 삭제
 * 6. 요청값 Validation 및 존재하지 않는 리소스 예외
 *
 * TODO: Gateway 인증 및 권한 전달 방식 확정 후 권한 테스트 추가
 * TODO: user-service FeignClient 연동 후 사용자 조회 성공·실패 테스트 추가
 * TODO: 실제 Slack API 연동 후 외부 API 성공·실패 테스트 분리
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Slack 메시지 API 통합 테스트")
class SlackMessageApiIntegrationTest {

    private static final UUID ORDER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID HUB_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final UUID RECEIVER_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");
    private static final UUID SECOND_ORDER_ID = UUID.fromString("44444444-4444-4444-4444-444444444444");
    private static final UUID SECOND_HUB_ID = UUID.fromString("55555555-5555-5555-5555-555555555555");
    private static final UUID SECOND_RECEIVER_ID = UUID.fromString("66666666-6666-6666-6666-666666666666");
    private static final UUID TEMPORARY_DELETED_BY = UUID.fromString("00000000-0000-0000-0000-000000000001");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private SlackMessageRepository slackMessageRepository;

    /**
     * 각 테스트가 서로 영향을 주지 않도록 실행 전에 Slack 메시지 데이터를 모두 삭제한다.
     *
     * 논리 삭제된 데이터도 실제 테이블에는 남아 있으므로
     * deleteAllInBatch()를 사용해 테스트 데이터를 물리적으로 정리한다.
     */
    @BeforeEach
    void setUp() {
        slackMessageRepository.deleteAllInBatch();
    }

    @Nested
    @DisplayName("Slack 메시지 발송")
    class CreateSlackMessage {

        @Test
        @DisplayName("유효한 요청이면 Slack 메시지를 발송하고 SENT 상태로 저장한다")
        void createSlackMessage_success() throws Exception {
            // given
            String requestBody = createRequestBody(ORDER_ID, HUB_ID, RECEIVER_ID, "주문 상품이 출고되어 배송을 시작합니다.");

            // when
            String responseBody = mockMvc.perform(
                            post("/api/v1/slack-messages")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(requestBody)
                    )
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.code").value(201))
                    .andExpect(jsonPath("$.data.slackMessageId").exists())
                    .andExpect(jsonPath("$.data.status").value("SENT"))
                    .andExpect(jsonPath("$.data.sentAt").exists())
                    .andReturn()
                    .getResponse()
                    .getContentAsString();

            /*
             * 생성 응답에서 slackMessageId를 추출한 뒤,
             * DB에 저장된 발송 결과까지 함께 확인한다.
             */
            UUID slackMessageId = extractSlackMessageId(responseBody);

            // then
            SlackMessage slackMessage = findSlackMessage(slackMessageId);

            assertThat(slackMessage.getOrderId()).isEqualTo(ORDER_ID);
            assertThat(slackMessage.getHubId()).isEqualTo(HUB_ID);
            assertThat(slackMessage.getReceiverId()).isEqualTo(RECEIVER_ID);
            assertThat(slackMessage.getMessage()).isEqualTo("주문 상품이 출고되어 배송을 시작합니다.");
            assertThat(slackMessage.getStatus()).isEqualTo(SlackMessageStatus.SENT);

            /*
             * 현재는 user-service 대신 임시 사용자 정보를 생성하고,
             * TemporarySlackClient가 임시 Slack 식별자를 반환한다.
             */
            assertThat(slackMessage.getReceiverName()).isNotBlank();
            assertThat(slackMessage.getSlackUserId()).isNotBlank();
            assertThat(slackMessage.getChannelId()).isNotBlank();
            assertThat(slackMessage.getSlackTs()).isNotBlank();
            assertThat(slackMessage.getSentAt()).isNotNull();
            assertThat(slackMessage.getFailureReason()).isNull();
        }

        @Test
        @DisplayName("메시지가 공백이면 400을 반환하고 데이터는 저장되지 않는다")
        void createSlackMessage_blankMessage() throws Exception {
            // given
            String requestBody = createRequestBody(ORDER_ID, HUB_ID, RECEIVER_ID, "   ");

            // when & then
            mockMvc.perform(
                            post("/api/v1/slack-messages")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(requestBody)
                    )
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.code").value(400))
                    .andExpect(jsonPath("$.error").value("INVALID_INPUT_VALUE"));

            assertThat(slackMessageRepository.count()).isZero();
        }

        @Test
        @DisplayName("수신자 ID가 없으면 400을 반환하고 데이터는 저장되지 않는다")
        void createSlackMessage_missingReceiverId() throws Exception {
            // given
            String requestBody = """
                    {
                      "orderId": "%s",
                      "hubId": "%s",
                      "message": "수신자 ID 누락 테스트 메시지"
                    }
                    """.formatted(ORDER_ID, HUB_ID);

            // when & then
            mockMvc.perform(
                            post("/api/v1/slack-messages")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(requestBody)
                    )
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.code").value(400))
                    .andExpect(jsonPath("$.error").value("INVALID_INPUT_VALUE"));

            assertThat(slackMessageRepository.count()).isZero();
        }
    }

    @Nested
    @DisplayName("Slack 메시지 단건 조회")
    class GetSlackMessage {

        @Test
        @DisplayName("삭제되지 않은 Slack 메시지를 ID로 조회할 수 있다")
        void getSlackMessage_success() throws Exception {
            // given
            UUID slackMessageId = createSlackMessage(ORDER_ID, HUB_ID, RECEIVER_ID, "Slack 메시지 단건 조회 테스트");

            // when & then
            mockMvc.perform(get("/api/v1/slack-messages/{slackMessageId}", slackMessageId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.slackMessageId").value(slackMessageId.toString()))
                    .andExpect(jsonPath("$.data.orderId").value(ORDER_ID.toString()))
                    .andExpect(jsonPath("$.data.hubId").value(HUB_ID.toString()))
                    .andExpect(jsonPath("$.data.receiverId").value(RECEIVER_ID.toString()))
                    .andExpect(jsonPath("$.data.receiverName").isNotEmpty())
                    .andExpect(jsonPath("$.data.slackUserId").isNotEmpty())
                    .andExpect(jsonPath("$.data.message").value("Slack 메시지 단건 조회 테스트"))
                    .andExpect(jsonPath("$.data.status").value("SENT"))
                    .andExpect(jsonPath("$.data.failureReason").isEmpty())
                    .andExpect(jsonPath("$.data.channelId").isNotEmpty())
                    .andExpect(jsonPath("$.data.slackTs").isNotEmpty())
                    .andExpect(jsonPath("$.data.sentAt").exists());
        }

        @Test
        @DisplayName("존재하지 않는 Slack 메시지를 조회하면 404를 반환한다")
        void getSlackMessage_notFound() throws Exception {
            // given
            UUID unknownSlackMessageId = UUID.randomUUID();

            // when & then
            mockMvc.perform(get("/api/v1/slack-messages/{slackMessageId}", unknownSlackMessageId))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.code").value(404))
                    .andExpect(jsonPath("$.data").isEmpty())
                    .andExpect(jsonPath("$.error").value("SLACK_MESSAGE_NOT_FOUND"));
        }
    }

    @Nested
    @DisplayName("Slack 메시지 목록 및 검색")
    class SearchSlackMessages {

        @Test
        @DisplayName("삭제되지 않은 Slack 메시지 목록을 페이징하여 조회한다")
        void searchSlackMessages_success() throws Exception {
            // given
            createSlackMessage(ORDER_ID, HUB_ID, RECEIVER_ID, "첫 번째 메시지");
            createSlackMessage(SECOND_ORDER_ID, HUB_ID, RECEIVER_ID, "두 번째 메시지");
            createSlackMessage(UUID.randomUUID(), SECOND_HUB_ID, SECOND_RECEIVER_ID, "세 번째 메시지");

            // when & then
            mockMvc.perform(
                            get("/api/v1/slack-messages")
                                    .param("page", "0")
                                    .param("size", "2")
                    )
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.content.length()").value(2))
                    .andExpect(jsonPath("$.data.totalElements").value(3))
                    .andExpect(jsonPath("$.data.totalPages").value(2))
                    .andExpect(jsonPath("$.data.first").value(true))
                    .andExpect(jsonPath("$.data.last").value(false));
        }

        @Test
        @DisplayName("orderId로 Slack 메시지를 검색할 수 있다")
        void searchSlackMessages_byOrderId() throws Exception {
            // given
            createSlackMessage(ORDER_ID, HUB_ID, RECEIVER_ID, "주문 ID 검색 대상 메시지");
            createSlackMessage(SECOND_ORDER_ID, HUB_ID, RECEIVER_ID, "검색에서 제외될 메시지");

            // when & then
            mockMvc.perform(
                            get("/api/v1/slack-messages")
                                    .param("orderId", ORDER_ID.toString())
                                    .param("page", "0")
                                    .param("size", "10")
                    )
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.content.length()").value(1))
                    .andExpect(jsonPath("$.data.totalElements").value(1))
                    .andExpect(jsonPath("$.data.content[0].orderId").value(ORDER_ID.toString()));
        }

        @Test
        @DisplayName("receiverName의 일부로 Slack 메시지를 검색할 수 있다")
        void searchSlackMessages_byReceiverName() throws Exception {
            // given
            UUID slackMessageId = createSlackMessage(ORDER_ID, HUB_ID, RECEIVER_ID, "수신자 이름 검색 테스트");
            SlackMessage slackMessage = findSlackMessage(slackMessageId);
            String receiverNameKeyword = slackMessage.getReceiverName().substring(0, 2);

            // when & then
            mockMvc.perform(
                            get("/api/v1/slack-messages")
                                    .param("receiverName", receiverNameKeyword)
                                    .param("page", "0")
                                    .param("size", "10")
                    )
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.content.length()").value(1))
                    .andExpect(jsonPath("$.data.totalElements").value(1))
                    .andExpect(jsonPath("$.data.content[0].slackMessageId").value(slackMessageId.toString()));
        }

        @Test
        @DisplayName("status로 Slack 메시지를 검색할 수 있다")
        void searchSlackMessages_byStatus() throws Exception {
            // given
            UUID sentMessageId = createSlackMessage(ORDER_ID, HUB_ID, RECEIVER_ID, "발송 상태 검색 테스트");

            // when & then
            mockMvc.perform(
                            get("/api/v1/slack-messages")
                                    .param("status", "SENT")
                                    .param("page", "0")
                                    .param("size", "10")
                    )
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.content.length()").value(1))
                    .andExpect(jsonPath("$.data.totalElements").value(1))
                    .andExpect(jsonPath("$.data.content[0].slackMessageId").value(sentMessageId.toString()))
                    .andExpect(jsonPath("$.data.content[0].status").value("SENT"));
        }
    }

    @Nested
    @DisplayName("Slack 메시지 수정")
    class UpdateSlackMessage {

        @Test
        @DisplayName("SENT 상태의 Slack 메시지를 수정하면 MODIFIED 상태로 변경된다")
        void updateSlackMessage_sentToModified() throws Exception {
            // given
            UUID slackMessageId = createSlackMessage(ORDER_ID, HUB_ID, RECEIVER_ID, "수정 전 메시지");

            String requestBody = """
                    {
                      "message": "수정된 Slack 메시지입니다."
                    }
                    """;

            // when & then
            mockMvc.perform(
                            patch("/api/v1/slack-messages/{slackMessageId}", slackMessageId)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(requestBody)
                    )
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.slackMessageId").value(slackMessageId.toString()))
                    .andExpect(jsonPath("$.data.message").value("수정된 Slack 메시지입니다."))
                    .andExpect(jsonPath("$.data.status").value("MODIFIED"))
                    .andExpect(jsonPath("$.data.updatedAt").exists());

            //* TemporarySlackClient 수정이 성공한 뒤 실제 DB의 message와 status도 변경됐는지 확인한다.
            SlackMessage updatedSlackMessage = findSlackMessage(slackMessageId);

            assertThat(updatedSlackMessage.getMessage()).isEqualTo("수정된 Slack 메시지입니다.");
            assertThat(updatedSlackMessage.getStatus()).isEqualTo(SlackMessageStatus.MODIFIED);
            assertThat(updatedSlackMessage.getFailureReason()).isNull();
        }

        @Test
        @DisplayName("MODIFIED 상태의 Slack 메시지를 다시 수정할 수 있다")
        void updateSlackMessage_modifiedToModified() throws Exception {
            // given
            UUID slackMessageId = createSlackMessage(ORDER_ID, HUB_ID, RECEIVER_ID, "최초 메시지");
            updateSlackMessage(slackMessageId, "첫 번째 수정 메시지");

            String requestBody = """
                    {
                      "message": "두 번째 수정 메시지"
                    }
                    """;

            // when & then
            mockMvc.perform(
                            patch("/api/v1/slack-messages/{slackMessageId}", slackMessageId)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(requestBody)
                    )
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.message").value("두 번째 수정 메시지"))
                    .andExpect(jsonPath("$.data.status").value("MODIFIED"));

            SlackMessage updatedSlackMessage = findSlackMessage(slackMessageId);

            assertThat(updatedSlackMessage.getMessage()).isEqualTo("두 번째 수정 메시지");
            assertThat(updatedSlackMessage.getStatus()).isEqualTo(SlackMessageStatus.MODIFIED);
        }

        @Test
        @DisplayName("수정할 메시지가 공백이면 400을 반환하고 기존 내용은 유지된다")
        void updateSlackMessage_blankMessage() throws Exception {
            // given
            UUID slackMessageId = createSlackMessage(ORDER_ID, HUB_ID, RECEIVER_ID, "변경되면 안 되는 메시지");

            String requestBody = """
                    {
                      "message": "   "
                    }
                    """;

            // when & then
            mockMvc.perform(
                            patch("/api/v1/slack-messages/{slackMessageId}", slackMessageId)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(requestBody)
                    )
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error").value("INVALID_INPUT_VALUE"));

            SlackMessage unchangedSlackMessage = findSlackMessage(slackMessageId);

            assertThat(unchangedSlackMessage.getMessage()).isEqualTo("변경되면 안 되는 메시지");
            assertThat(unchangedSlackMessage.getStatus()).isEqualTo(SlackMessageStatus.SENT);
        }

        @Test
        @DisplayName("존재하지 않는 Slack 메시지를 수정하면 404를 반환한다")
        void updateSlackMessage_notFound() throws Exception {
            // given
            UUID unknownSlackMessageId = UUID.randomUUID();

            String requestBody = """
                    {
                      "message": "수정 요청 메시지"
                    }
                    """;

            // when & then
            mockMvc.perform(
                            patch("/api/v1/slack-messages/{slackMessageId}", unknownSlackMessageId)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(requestBody)
                    )
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error").value("SLACK_MESSAGE_NOT_FOUND"));
        }
    }

    @Nested
    @DisplayName("Slack 메시지 발송 이력 삭제")
    class DeleteSlackMessage {

        @Test
        @DisplayName("Slack 메시지를 삭제하면 발송 이력만 논리 삭제된다")
        void deleteSlackMessage_success() throws Exception {
            // given
            UUID slackMessageId = createSlackMessage(ORDER_ID, HUB_ID, RECEIVER_ID, "논리 삭제 테스트 메시지");

            // when
            mockMvc.perform(delete("/api/v1/slack-messages/{slackMessageId}", slackMessageId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.code").value(200));

            // then
            /*
             * Repository의 기본 findById()로 조회하면 실제 행은 남아 있어야 한다.
             * deletedAt과 deletedBy가 기록됐는지 확인해 논리 삭제를 검증한다.
             */
            SlackMessage deletedSlackMessage = slackMessageRepository.findById(slackMessageId).orElseThrow();

            assertThat(deletedSlackMessage.getDeletedAt()).isNotNull();
            assertThat(deletedSlackMessage.getDeletedBy()).isEqualTo(TEMPORARY_DELETED_BY);
            assertThat(slackMessageRepository.count()).isEqualTo(1);
        }

        @Test
        @DisplayName("논리 삭제된 Slack 메시지는 단건 조회할 수 없다")
        void getSlackMessage_deletedMessageNotFound() throws Exception {
            // given
            UUID slackMessageId = createSlackMessage(ORDER_ID, HUB_ID, RECEIVER_ID, "삭제 후 조회 테스트");
            deleteSlackMessage(slackMessageId);

            // when & then
            mockMvc.perform(get("/api/v1/slack-messages/{slackMessageId}", slackMessageId))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error").value("SLACK_MESSAGE_NOT_FOUND"));
        }

        @Test
        @DisplayName("논리 삭제된 Slack 메시지는 목록과 검색 결과에서 제외된다")
        void searchSlackMessages_excludesDeletedMessage() throws Exception {
            // given
            UUID deletedMessageId = createSlackMessage(ORDER_ID, HUB_ID, RECEIVER_ID, "삭제될 메시지");
            UUID activeMessageId = createSlackMessage(SECOND_ORDER_ID, HUB_ID, RECEIVER_ID, "유지될 메시지");

            deleteSlackMessage(deletedMessageId);

            // when & then
            mockMvc.perform(
                            get("/api/v1/slack-messages")
                                    .param("page", "0")
                                    .param("size", "10")
                    )
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.content.length()").value(1))
                    .andExpect(jsonPath("$.data.totalElements").value(1))
                    .andExpect(jsonPath("$.data.content[0].slackMessageId").value(activeMessageId.toString()));
        }

        @Test
        @DisplayName("존재하지 않는 Slack 메시지를 삭제하면 404를 반환한다")
        void deleteSlackMessage_notFound() throws Exception {
            // given
            UUID unknownSlackMessageId = UUID.randomUUID();

            // when & then
            mockMvc.perform(delete("/api/v1/slack-messages/{slackMessageId}", unknownSlackMessageId))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error").value("SLACK_MESSAGE_NOT_FOUND"));
        }
    }

    // 테스트에서 반복되는 Slack 메시지 생성 API를 호출하고 생성된 slackMessageId를 반환한다.
    private UUID createSlackMessage(UUID orderId, UUID hubId, UUID receiverId, String message) throws Exception {
        String requestBody = createRequestBody(orderId, hubId, receiverId, message);

        String responseBody = mockMvc.perform(
                        post("/api/v1/slack-messages")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return extractSlackMessageId(responseBody);
    }

    // 수정 API를 미리 호출해 테스트 데이터를 MODIFIED 상태로 만든다.

    private void updateSlackMessage(UUID slackMessageId, String message) throws Exception {
        String requestBody = """
                {
                  "message": "%s"
                }
                """.formatted(message);

        mockMvc.perform(
                        patch("/api/v1/slack-messages/{slackMessageId}", slackMessageId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isOk());
    }

    // 삭제 API를 호출해 테스트 데이터를 논리 삭제 상태로 만든다.
    private void deleteSlackMessage(UUID slackMessageId) throws Exception {
        mockMvc.perform(delete("/api/v1/slack-messages/{slackMessageId}", slackMessageId))
                .andExpect(status().isOk());
    }

    // 발송 요청 JSON을 생성한다.
    private String createRequestBody(UUID orderId, UUID hubId, UUID receiverId, String message) {
        return """
                {
                  "orderId": "%s",
                  "hubId": "%s",
                  "receiverId": "%s",
                  "message": "%s"
                }
                """.formatted(orderId, hubId, receiverId, message);
    }

    // 생성 API 응답의 data.slackMessageId를 UUID로 변환한다.
    private UUID extractSlackMessageId(String responseBody) throws Exception {
        JsonNode root = objectMapper.readTree(responseBody);
        return UUID.fromString(root.path("data").path("slackMessageId").asText());
    }

    /**
     * Slack 메시지 ID로 DB 데이터를 조회한다.
     *
     * 논리 삭제 여부와 관계없이 데이터를 확인할 수 있도록 JpaRepository의 기본 findById()를 사용한다.
     */
    private SlackMessage findSlackMessage(UUID slackMessageId) {
        return slackMessageRepository.findById(slackMessageId).orElseThrow();
    }
}
