package com.msa7.v1.delivery.app.consumer;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.msa7.v1.delivery.app.DeliveryService;
import com.msa7.v1.delivery.presentation.dto.payload.OrderCreatedEvent;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class DeliveryEventConsumer {
	private final DeliveryService deliveryService;
	private final ObjectMapper objectMapper;

	// 주문 도메인에서 던진 이벤트 수신
	@RabbitListener(queues = "order-created-queue")
	public void onOrderCreated(String message) {
		try {
			OrderCreatedEvent event = objectMapper.readValue(message, OrderCreatedEvent.class);

			// 배송 서비스의 SAGA 전용 메서드 호출
			deliveryService.createDeliveryFromOrder(
				event.orderId(),
				event.startHubId(),
				event.endHubId(),
				event.receiverSlackId(),
				event.destinationAddress()
			);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
