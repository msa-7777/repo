package com.msa7.v1.order.presentation.listener;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import com.msa7.v1.order.app.OrderService;
import com.msa7.v1.order.presentation.dto.payload.DeliveryCreatedEvent;
import com.msa7.v1.order.presentation.dto.payload.DeliveryFailedEvent;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class OrderEventListener {
	private final OrderService orderService;

	@RabbitListener(queues = "delivery-success-queue")
	public void onDeliveryCreated(DeliveryCreatedEvent event) {
		orderService.startDelivery(event.orderId());

	}

	@RabbitListener(queues = "delivery-fail-queue")
	public void onDeliveryFailed(DeliveryFailedEvent event) {
		orderService.compensateOrder(event.orderId());
	}
}
