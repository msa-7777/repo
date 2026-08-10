package com.msa7.v1.delivery.app;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.msa7.v1.delivery.domain.aggregateDelivery.Delivery;
import com.msa7.v1.delivery.domain.aggregateDelivery.DeliveryRouteRecord;
import com.msa7.v1.delivery.domain.aggregateManager.DeliveryManager;
import com.msa7.v1.delivery.domain.repo.DeliveryManagerRepo;
import com.msa7.v1.delivery.domain.repo.DeliveryRepo;
import com.msa7.v1.delivery.domain.vo.ManagerType;
import com.msa7.v1.delivery.infra.feign.HubClient;
import com.msa7.v1.delivery.infra.feign.UserClient;
import com.msa7.v1.delivery.presentation.dto.HubRouteResponse;


import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DeliveryService {

	private final DeliveryRepo deliveryRepo;
	private final UserClient userClient;
	private final HubClient hubClient;

	@Transactional
	public UUID createDelivery(UUID orderId, UUID startHubId, UUID endHubId, String destinationAddress,
		String receiverName, UUID receiverSlackId, UUID companyManagerId) {

		// 1. 도메인 객체 생성
		Delivery delivery = Delivery.create(orderId, startHubId, endHubId, receiverName,destinationAddress,receiverSlackId, companyManagerId);

		// 2. 허브 간 경로 생성 및 담당자 순차 배정 (Round-Robin 가정)
		HubRouteResponse response = hubClient.getRouteInfo(startHubId, endHubId);

		// 순차 배정을 위해 다음 담당자 조회 (예시: 이전 할당 Seq 상태 캐싱/조회 필요)
		DeliveryManager nextHubManager = managerRepo
			.findNextAvailableManager(startHubId, ManagerType.HUB_STAFF, -1)
			.orElseThrow(() -> new IllegalStateException("배정 가능한 허브 담당자가 없습니다."));

		// 3. 경로 기록 생성
		DeliveryRouteRecord route = DeliveryRouteRecord.create(
			1, response.startHubId(), response.endHubId(),
			response.estimatedDistance(), response.estimatedTime(),
			nextHubManager.getId()
		);
		delivery.assignRoutes(List.of(route));

		// 4. 최종 업체 배송 담당자 배정 (엔티티 필드)
		DeliveryManager nextCompanyManager = managerRepo
			.findNextAvailableManager(endHubId, ManagerType.COMPANY_STAFF, -1)
			.orElseThrow(() -> new IllegalStateException("배정 가능한 업체 담당자가 없습니다."));

		delivery.assignCompanyManager(nextCompanyManager.getId());

		return deliveryRepo.save(delivery).getId();
	}


}
