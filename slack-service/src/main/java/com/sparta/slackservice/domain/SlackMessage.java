package com.sparta.slackservice.domain;

import com.sparta.slackservice.global.audit.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Entity
@Table(name = "p_slack_messages")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SlackMessage extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "slack_message_id")
    private UUID id;

    @Column(name = "order_id", nullable = false)
    private UUID orderId;

    @Column(name = "hub_id", nullable = false)
    private UUID hubId;

    @Column(name = "receiver_id", nullable = false)
    private UUID receiverId;

    @Column(name = "receiver_name", length = 100)
    private String receiverName;

    @Column(name = "slack_user_id", length = 50)
    private String slackUserId;

    @Column(name = "message", columnDefinition = "TEXT", nullable = false)
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private SlackMessageStatus status;

    @Column(name = "failure_reason", length = 50)
    private String failureReason;

    @Column(name = "channel_id", length = 100)
    private String channelId;

    @Column(name = "slack_ts", length = 50)
    private String slackTs;

    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    private SlackMessage(
            UUID orderId,
            UUID hubId,
            UUID receiverId,
            String message
    ) {
        this.orderId = orderId;
        this.hubId = hubId;
        this.receiverId = receiverId;
        this.message = message;
        this.status = SlackMessageStatus.FAILED;
    }

    public static SlackMessage create(
            UUID orderId,
            UUID hubId,
            UUID receiverId,
            String message
    ) {
        return new SlackMessage(
                orderId,
                hubId,
                receiverId,
                message
        );
    }

    public void assignReceiver(
            String receiverName,
            String slackUserId
    ) {
        this.receiverName = receiverName;
        this.slackUserId = slackUserId;
    }

    public void markAsSent(
            String channelId,
            String slackTs,
            LocalDateTime sentAt
    ) {
        this.channelId = channelId;
        this.slackTs = slackTs;
        this.sentAt = sentAt;
        this.status = SlackMessageStatus.SENT;
        this.failureReason = null;
    }

    public void markAsFailed(SlackFailureReason reason) {
        this.status = SlackMessageStatus.FAILED;
        this.failureReason = reason.getMessage();
        this.channelId = null;
        this.slackTs = null;
        this.sentAt = null;
    }

    public void updateMessage(String message) {
        validateModifiable();

        this.message = message;
        this.status = SlackMessageStatus.MODIFIED;
        this.failureReason = null;
    }

    public void delete(UUID deletedBy) {
        super.delete(deletedBy);
    }

    private void validateModifiable() {
        if (status != SlackMessageStatus.SENT
                && status != SlackMessageStatus.MODIFIED) {
            throw new IllegalStateException(
                    "발송에 성공한 Slack 메시지만 수정할 수 있습니다."
            );
        }

        if (channelId == null || slackTs == null) {
            throw new IllegalStateException(
                    "Slack 메시지 식별 정보가 존재하지 않습니다."
            );
        }
    }

    public boolean isModifiable() {
        return status == SlackMessageStatus.SENT
                || status == SlackMessageStatus.MODIFIED;
    }
}