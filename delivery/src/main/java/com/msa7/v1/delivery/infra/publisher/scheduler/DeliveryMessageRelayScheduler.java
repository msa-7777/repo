package com.msa7.v1.delivery.infra.publisher.scheduler;

import java.util.List;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.msa7.v1.delivery.infra.outobx.DeliveryOutboxEvent;
import com.msa7.v1.delivery.infra.outobx.DeliveryOutboxEventRepo;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class DeliveryMessageRelayScheduler {
	private final DeliveryOutboxEventRepo outboxEventRepo;
	private final RabbitTemplate rabbitTemplate;

	@Scheduled(fixedDelay = 5000)
	@Transactional
	public void publishOutboxEvents() {
		List<DeliveryOutboxEvent> unPublishedEvents = outboxEventRepo.findAllByPublishedFalse();
		for (DeliveryOutboxEvent event : unPublishedEvents) {
			rabbitTemplate.convertAndSend("delivery-exchange", "delivery." + event.getEventType(), event.getPayload());
			event.markAsPublished();
		}
	}
}
