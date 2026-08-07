package com.msa7.v1.order.app;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.msa7.v1.order.domain.aggregate.Order;
import com.msa7.v1.order.domain.repo.OrderRepo;
import com.msa7.v1.order.infra.feign.ProductService;
import com.msa7.v1.order.infra.feign.UserClient;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderService {

	private final OrderRepo orderRepo;
	// private final UserClient userClient;
	// private final ProductService productService;

	@Transactional
	public Order createOrder(UUID receiverId, UUID productId, Integer quantity, String requests) {
		// 추가 로직: 주문 요청자의 정보 검증 (동기 호출된 유저 정보)

		// 추가 로직: 상품 재고 확인 (동기 호출된 상품 재고 및 최단거리 허브 배정 정보)

		// 추가 로직: 재고 차감(동기 호출)

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
		orderRepo.save(order); // 상태 업데이트해서 softDelete
	}

}
