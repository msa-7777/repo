package com.msa7.v1.order.app;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.msa7.v1.order.domain.aggregate.Order;
import com.msa7.v1.order.domain.repo.OrderRepo;
import com.msa7.v1.order.infra.feign.InventoryClient;
import com.msa7.v1.order.infra.publisher.OrderEventPub;
import com.msa7.v1.order.presentation.dto.payload.OrderCreatedEvent;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderService {

	private final OrderRepo orderRepo;
	private final InventoryClient inventoryClient;
	private final OrderEventPub orderEventPub;

	@Transactional
	public Order createOrder(UUID receiverId, UUID productId, Integer quantity, String requests) {
		// 재고 확인 inventory에서 요청(필요에 따라 동기 유지 또는 이벤트 전환)
		inventoryClient.verifyInventory(productId, quantity);

		// 도메인 객체 생성 및 저장
		Order order = Order.create(receiverId, productId, quantity, requests);
		Order savedOrder = orderRepo.save(order);

		// 배송 생성 요청
		OrderCreatedEvent event = new OrderCreatedEvent(savedOrder.getId(), receiverId, requests);
		orderEventPub.publishOrderCreated(event);

		return savedOrder;
	}


	@Transactional
	public Order getOrder(UUID orderId) {
		return orderRepo.findById(orderId).orElseThrow(() -> new IllegalArgumentException("Order not found"));
	}

	@Transactional
	public Order updateOrder(UUID orderId, Integer quantity, String requests) {
		Order order = getOrder(orderId);
		order.update(quantity, requests);
		return orderRepo.save(order);
	}

	@Transactional
	public void cancelOrder(UUID orderId) {
		Order order = getOrder(orderId);
		order.cancel();
		orderRepo.save(order);
	}

	@Transactional
	public void deleteOrder(UUID orderId) {
		Order order = getOrder(orderId);
		order.delete();
		orderRepo.save(order); // 상태 업데이트해서 softDelete
	}

	public void startDelivery(UUID uuid) {
	}

	@Transactional
	public void compensateOrder(UUID orderId) {
		Order order = getOrder(orderId);
		order.cancel();
		orderRepo.save(order);
	}
}
