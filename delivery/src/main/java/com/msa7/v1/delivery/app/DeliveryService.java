package com.msa7.v1.delivery.app;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.msa7.v1.delivery.domain.aggregateDelivery.Delivery;
import com.msa7.v1.delivery.domain.aggregateDelivery.DeliveryRouteRecord;
import com.msa7.v1.delivery.domain.repo.DeliveryRepo;
import com.msa7.v1.delivery.domain.vo.DeliveryStatus;
import com.msa7.v1.delivery.infra.feign.HubClient;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DeliveryService {

	private final DeliveryRepo deliveryRepo;
	private final HubClient hubClient;

	// 배송 최초 생성 (주문 생성 직후 동기 호출된다고 가정 - Phase 1)
	@Transactional
	public UUID createDelivery(UUID orderId, UUID receiverId, String destinationAddress) {
		// 1 도메인 객체 생성
		Delivery delivery = Delivery.create(orderId, receiverId, destinationAddress);

		// 2 외부 서비스(HubClient) 호출 등을 통해 경로 계산 (Mock 로직)

		// 3 경로 설정
		DeliveryRouteRecord route = DeliveryRouteRecord.create(
			1, UUID.randomUUID(), UUID.randomUUID(), 100L, 60L, UUID.randomUUID()
		);
		delivery.assignRoutes(List.of(route));

		// 4 저장 (Mapper를 통해 Domain -> Entity 변환 후 JPA save)
		deliveryRepo.save(delivery);
		return delivery.getId();
	}

	@Transactional
	public void updateDeliveryStatus(UUID deliveryId, DeliveryStatus status, UUID currentHubId) {
		Delivery delivery = deliveryRepo.findById(deliveryId)
			.orElseThrow(() -> new IllegalArgumentException("배송을 찾을 수 없습니다."));

		// 도메인 로직 위임
		delivery.updateStatus(status, currentHubId);

		deliveryRepo.save(delivery); // 변경 감지(더티 체킹) 또는 Mapper 갱신
	}
}
