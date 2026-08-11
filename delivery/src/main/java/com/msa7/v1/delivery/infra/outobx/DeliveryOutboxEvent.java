package com.msa7.v1.delivery.infra.outobx;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "p_delivery_outbox_events")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DeliveryOutboxEvent {
	@Id
	private UUID eventId = UUID.randomUUID();
	private String aggregateType;
	private String aggregateId;
	private String eventType;

	@Column(columnDefinition = "jsonb")
	private String payload;
	private LocalDateTime createdAt = LocalDateTime.now();
	private boolean published = false;

	public DeliveryOutboxEvent(String aggregateType, String aggregateId, String eventType, String payload) {
		this.aggregateType = aggregateType;
		this.aggregateId = aggregateId;
		this.eventType = eventType;
		this.payload = payload;
	}
	public void markAsPublished() { this.published = true; }
	public String getEventType() { return eventType; }
	public String getPayload() { return payload; }
}
