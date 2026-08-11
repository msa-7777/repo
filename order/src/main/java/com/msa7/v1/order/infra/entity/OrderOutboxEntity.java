package com.msa7.v1.order.infra.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "p_order_outbox")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderOutboxEntity {
	@Id
	private UUID id;
	private String aggregateType;
	private String aggregateId;
	private String eventType;
	@Column(columnDefinition = "TEXT")
	private String payload;
	private boolean published;
	private LocalDateTime createdAt;

	public OrderOutboxEntity(String aggregateType, String aggregateId, String eventType, String payload) {
		this.id = UUID.randomUUID();
		this.aggregateType = aggregateType;
		this.aggregateId = aggregateId;
		this.eventType = eventType;
		this.payload = payload;
		this.published = false;
		this.createdAt = LocalDateTime.now();
	}
	public void markAsPublished() {
		this.published = true;
	}
}
