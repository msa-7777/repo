package com.msa7.v1.order.infra.outbox;

import java.util.List;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.msa7.v1.order.infra.entity.OrderOutboxEntity;
import com.msa7.v1.order.infra.repo.OrderOutboxRepository;

import lombok.RequiredArgsConstructor;

// Db->> RabbitMq
@Component
@RequiredArgsConstructor
public class OrderOutboxRelay {
	private final OrderOutboxRepository orderOutboxRepo;
	private final RabbitTemplate rabbitTemplate;

	@Scheduled(fixedDelay = 5000)
	@Transactional
	public void publishOutboxMessages() {
		List<OrderOutboxEntity> messages = orderOutboxRepo.findByPublishedFalse();

		for (OrderOutboxEntity message : messages) {
			rabbitTemplate.convertAndSend("order-exchange", "order.created", message.getPayload());
			message.markAsPublished();
		}
	}
}
