package com.msa7.v1.order.app;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.msa7.v1.order.domain.aggregate.Order;
import com.msa7.v1.order.domain.repo.OrderRepo;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderService {

	private final OrderRepo orderRepo;

	@Transactional
	public Order createOrder(UUID receiverId, UUID productId, Integer quantity, String requests) {
		Order order = Order.create(receiverId, productId, quantity, requests);
		return orderRepo.save(order);
	}

	// cqurs 분리 예정이라 단건 조회 및 다중조건 조회 구현 구체화 할 예정
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
		orderRepo.delete(order);
	}

}
