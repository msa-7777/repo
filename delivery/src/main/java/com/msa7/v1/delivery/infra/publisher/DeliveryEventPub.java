package com.msa7.v1.delivery.infra.publisher;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import com.msa7.v1.delivery.presentation.dto.payload.DeliveryCreatedEvent;
import com.msa7.v1.delivery.presentation.dto.payload.DeliveryFailedEvent;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class DeliveryEventPub {
	private final RabbitTemplate rabbitTemplate;

	public void publishDeliverySuccess(DeliveryCreatedEvent event) {
		rabbitTemplate.convertAndSend("delivery-exchange", "delivery.success", event);
	}

	public void publishDeliveryFailure(DeliveryFailedEvent event) {
		rabbitTemplate.convertAndSend("delivery-exchange", "delivery.failure", event);
	}
}
