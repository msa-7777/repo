package com.msa7.v1.order.infra.outbox;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.msa7.v1.order.app.OrderService;
import com.msa7.v1.order.presentation.dto.payload.DeliveryCreatedEvent;
import com.msa7.v1.order.presentation.dto.payload.DeliveryFailedEvent;

import lombok.RequiredArgsConstructor;

// Mq-> Order saga 완료 or 시류패 처리
@Component
@RequiredArgsConstructor
public class OrderMessageListener {
	private final OrderService orderService;
	private final ObjectMapper objectMapper;

	@RabbitListener(queues = "delivery-created-queue")
	public void onDeliveryCreated(String payload) throws Exception {
		DeliveryCreatedEvent event = objectMapper.readValue(payload, DeliveryCreatedEvent.class);
		orderService.compeleteOrderSaga(event.orderId(), event.deliveryId());;
	}

	@RabbitListener(queues = "delivery-fail-queue")
	public void onDeliveryFailed(String payload) throws Exception {
		DeliveryFailedEvent event = objectMapper.readValue(payload, DeliveryFailedEvent.class);
		orderService.failOrderSaga(event.orderId(), event.reason());
	}

}
