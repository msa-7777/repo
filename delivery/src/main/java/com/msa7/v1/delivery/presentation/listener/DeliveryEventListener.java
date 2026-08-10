package com.msa7.v1.delivery.presentation.listener;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import com.msa7.v1.delivery.app.DeliveryService;
import com.msa7.v1.delivery.presentation.dto.payload.OrderCreatedEvent;

import lombok.RequiredArgsConstructor;

// @Component
// @RequiredArgsConstructor
// public class DeliveryEventListener {
// 	private final DeliveryService deliveryService;
//
// 	@RabbitListener(queues = "order-created-queue")
// 	public void onOrderCreated(OrderCreatedEvent event) {
// 		deliveryService.createDelivery(event);
// 	}
//
//
// }
