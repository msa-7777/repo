package com.msa7.v1.order.infra.publisher;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import com.msa7.v1.order.presentation.dto.payload.OrderCreatedEvent;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class OrderEventPub {
	private final RabbitTemplate rabbitTemplate;

	public void publishOrderCreated(OrderCreatedEvent event) {
		// 객체를 메시지로 변환하고 전송하는 rabbit메서드
		rabbitTemplate.convertAndSend("order-exchange", "order.created", event);
	}

}
