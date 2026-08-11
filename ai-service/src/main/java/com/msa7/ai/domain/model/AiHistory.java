package com.msa7.ai.domain.model;

import com.msa7.ai.global.audit.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Table(name = "p_ai_histories")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AiHistory extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "history_id", nullable = false, updatable = false)
    private UUID historyId;

    @Column(name = "order_id", nullable = false)
    private UUID orderId;

    @Column(name = "prompt_request", nullable = false, columnDefinition = "TEXT")
    private String promptRequest;

    @Column(name = "calculated_deadline", nullable = false)
    private LocalDateTime calculatedDeadline;

    @Column(name = "generated_message", nullable = false, columnDefinition = "TEXT")
    private String generatedMessage;

    @Column(name = "is_slack_notified", nullable = false)
    private Boolean isSlackNotified;

    private AiHistory(UUID orderId, String promptRequest, LocalDateTime calculatedDeadline, String generatedMessage, Boolean isSlackNotified) {
        this.orderId = orderId;
        this.promptRequest = promptRequest;
        this.calculatedDeadline = calculatedDeadline;
        this.generatedMessage = generatedMessage;
        this.isSlackNotified = isSlackNotified;
    }

    public static AiHistory create(UUID orderId, String promptRequest, LocalDateTime calculatedDeadline, String generatedMessage, Boolean isSlackNotified) {
        return new AiHistory(orderId, promptRequest, calculatedDeadline, generatedMessage, isSlackNotified);
    }

    public void updateSlackNotificationStatus(boolean status) {
        this.isSlackNotified = status;
    }

    public void delete(UUID deletedBy) {
        super.softDelete(deletedBy);
    }
}