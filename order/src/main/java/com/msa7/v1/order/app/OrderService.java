package com.msa7.v1.order.app;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.msa7.v1.order.infra.feign.DeliveryClient;
import com.msa7.v1.order.domain.aggregate.Order;
import com.msa7.v1.order.domain.repo.OrderRepo;
import com.msa7.v1.order.infra.feign.InventoryClient;
import com.msa7.v1.order.presentation.dto.onlycontoller.RestApiResponse;
import com.msa7.v1.order.presentation.dto.payload.DeliveryResponse;
import com.msa7.v1.order.presentation.dto.payload.OrderCreatedEvent;
import com.msa7.v1.order.presentation.dto.payload.OrderWithDeliveryDto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

	private final OrderRepo orderRepo;
	private final InventoryClient inventoryClient;
	private final DeliveryClient deliveryClient;

	@Transactional
	public Order createOrder(UUID receiverId, UUID productId, Integer quantity, String requests,
		UUID receiverSlackId, UUID startHubId, UUID endHubId, String destinationAddress
	) {
		// 재고 확인 inventory에서 요청(필요에 따라 동기 유지 또는 이벤트 전환)
		inventoryClient.verifyInventory(productId, quantity);
		// 도메인 생성 (이벤트 등록)
		Order order = Order.create(receiverId, productId, quantity, requests
		, receiverSlackId, startHubId, endHubId, destinationAddress);
		return orderRepo.save(order);
	}

	@Transactional
	public Order getOrder(UUID orderId) {
		return orderRepo.findById(orderId).orElseThrow(() -> new IllegalArgumentException("Order not found"));
	}

	@Transactional
	public void compeleteOrderSaga(UUID orderId, UUID deliveryId) {
		Order order = orderRepo.findById(orderId).orElseThrow(() -> new IllegalArgumentException("Order not found"));
		order.startDelivery(deliveryId);
		orderRepo.save(order);
	}

	@Transactional
	public void failOrderSaga(UUID orderId, String reason) {
		Order order = orderRepo.findById(orderId).orElseThrow(() -> new IllegalArgumentException("Order not found"));
		order.cancelOrder();
		orderRepo.save(order);
		// Todo: 시간이 남으면 인펜토리 서비스에 재고 복구 트랜잭션 이벤트 해보기
	}

	// 주문ID만 주면 배송상태를 알수 있는 api(필규님 요청)
	@Transactional(readOnly = true)
	public OrderWithDeliveryDto getOrderWithDeliveryStatus(UUID orderId) {
		Order order = orderRepo.findById(orderId).orElseThrow(() -> new IllegalArgumentException("Order not found"));
		if (order.getDeliveryId() == null) {
			return new OrderWithDeliveryDto(order, "배송 정보 없음");
		}
		String deliveryStatus;
		try {
			RestApiResponse<DeliveryResponse> response = deliveryClient.
				getDeliveryInfo(order.getDeliveryId());
				deliveryStatus = Optional.ofNullable(response)
					.map(RestApiResponse::data)
					.map(DeliveryResponse::status)
					.orElse("배송 정보 없음");
		} catch (Exception e){
			log.error("배송 조회 실패 deliveryId: {}", order.getDeliveryId(), e);
			deliveryStatus = "배송 정보 조회 실패";
		}
		return new OrderWithDeliveryDto(order, deliveryStatus);
	}

}
