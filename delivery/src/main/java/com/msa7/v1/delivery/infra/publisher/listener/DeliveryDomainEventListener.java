package com.msa7.v1.delivery.infra.publisher.listener;

import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.msa7.v1.delivery.infra.outobx.DeliveryOutboxEvent;
import com.msa7.v1.delivery.infra.outobx.DeliveryOutboxEventRepo;
import com.msa7.v1.delivery.presentation.dto.payload.DeliveryCreatedEvent;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class DeliveryDomainEventListener {
	private final DeliveryOutboxEventRepo outboxEventRepo;
	private final ObjectMapper objectMapper;

	@TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
	public void handleDeliveryCreatedEvent(DeliveryCreatedEvent event) {
		try {
			DeliveryOutboxEvent outboxEvent = new DeliveryOutboxEvent(
				"Delivery", event.deliveryId().toString(), "DeliveryCreatedEvent", objectMapper.writeValueAsString(event)
			);
			outboxEventRepo.save(outboxEvent);
		} catch (Exception e) {
			throw new RuntimeException("이벤트 직렬화 실패", e);
		}
	}
}
