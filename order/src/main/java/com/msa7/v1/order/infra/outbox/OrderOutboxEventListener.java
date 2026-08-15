package com.msa7.v1.order.infra.outbox;

import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.msa7.v1.order.infra.entity.OrderOutboxEntity;
import com.msa7.v1.order.infra.repo.OrderOutboxRepository;
import com.msa7.v1.order.presentation.dto.payload.OrderCreatedEvent;

import lombok.RequiredArgsConstructor;

// Domain Events -> Outbox listenr
@Component
@RequiredArgsConstructor
public class OrderOutboxEventListener {
	private final OrderOutboxRepository orderOutboxRepo;
	private final ObjectMapper objectMapper;

	@TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
	public void handleOrderCreatedEvent(OrderCreatedEvent event){
		try {
			String payload = objectMapper.writeValueAsString(event);
			OrderOutboxEntity entity = new OrderOutboxEntity("ORDER",
			event.orderId().toString(), "created", payload);
			orderOutboxRepo.save(entity);
		} catch (JsonProcessingException e) {
			throw new IllegalArgumentException("이벤트 직렬화 실패: "+ event.orderId(), e);
		}
	}
}
