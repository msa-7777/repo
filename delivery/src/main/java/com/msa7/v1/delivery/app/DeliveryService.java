package com.msa7.v1.delivery.app;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.msa7.v1.delivery.domain.aggregateDelivery.Delivery;
import com.msa7.v1.delivery.domain.aggregateDelivery.DeliveryRouteRecord;
import com.msa7.v1.delivery.domain.aggregateManager.DeliveryManager;
import com.msa7.v1.delivery.domain.repo.DeliveryManagerRepo;
import com.msa7.v1.delivery.domain.repo.DeliveryRepo;
import com.msa7.v1.delivery.domain.vo.DeliveryStatus;
import com.msa7.v1.delivery.domain.vo.ManagerType;
import com.msa7.v1.delivery.domain.vo.RouteStatus;
import com.msa7.v1.delivery.infra.feign.HubClient;
import com.msa7.v1.delivery.infra.feign.UserClient;
import com.msa7.v1.delivery.infra.repo.JpaDeliveryRouteRecordRepository;
import com.msa7.v1.delivery.presentation.dto.HubRouteResponse;
import com.msa7.v1.delivery.presentation.dto.payload.DeliveryResponse;
import com.msa7.v1.delivery.presentation.dto.payload.DeliveryRouteResponse;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DeliveryService {

	private final DeliveryRepo deliveryRepo;
	private final UserClient userClient;
	private final HubClient hubClient;
	private final JpaDeliveryRouteRecordRepository routeRepo;

	@Transactional
	public UUID createDelivery(UUID orderId, UUID startHubId, UUID endHubId, String destinationAddress,
		String receiverName, UUID receiverSlackId) {

		// 1. 업체 배송 담당자 할당
		UUID companyManagerId = userClient.getNextDeliveryManagerId(endHubId);

		// 2. 도메인 객체 생성
		Delivery delivery = Delivery.create(orderId, startHubId, endHubId, receiverName,destinationAddress,receiverSlackId, companyManagerId);

		// 3. 배송 경로 기록 일괄 생성 (최초 생성 시 전체 경로 세팅)
		HubRouteResponse hubRoute = hubClient.getRouteInfo(startHubId, endHubId);
		List<HubRouteResponse> hubRoutes = List.of(hubRoute);
		List<DeliveryRouteRecord> routes = new ArrayList<>();

		int sequence = 0;

		for (HubRouteResponse res : hubRoutes) {
			// 각 구간마다 담당할 허브 배송 담당자를 순차 할당 (UserClient 활용)
			UUID hubDeliveryManagerId = userClient.getNextDeliveryManagerId(res.startHubId());

			DeliveryRouteRecord route = DeliveryRouteRecord.create(
				sequence++,
				res.startHubId(),
				res.endHubId(),
				res.estimatedDistance(),
				res.estimatedTime(),
				hubDeliveryManagerId
			);
			routes.add(route);
		}

		// 4. 경로 할당 및 저장
		delivery.assignRoutes(routes);
		deliveryRepo.save(delivery);

		return delivery.getId();
	}

	@Transactional
	public void updateDeliveryStatus(UUID deliveryId, DeliveryStatus status) {
		Delivery delivery = deliveryRepo.findById(deliveryId)
			.orElseThrow(() -> new IllegalArgumentException("배송을 찾을 수 없습니다."));

		delivery.updateStatus(status);
		deliveryRepo.save(delivery);
	}

	@Transactional
	public void deleteDelivery(UUID id, UUID deletedBy) {
		deliveryRepo.deleteById(id, deletedBy);
	}

	// API 1: 허브 ID와 상태로 배송 경로 목록 조회
	@Transactional(readOnly = true)
	public List<DeliveryRouteResponse> getDeliveryRoutes(UUID hubId, RouteStatus status) {
		return routeRepo.findAllByStartHubIdAndStatusAndIsDeletedFalse(hubId, status).stream()
			.map(route -> new DeliveryRouteResponse(
				route.getId(),
				route.getSequence(),
				route.getStartHubId(),
				route.getEndHubId(),
				route.getStatus()
			))
			.toList();
	}

	@Transactional(readOnly = true)
	public DeliveryResponse getDeliveryInfo(UUID deliveryId) {
		Delivery delivery = deliveryRepo.findById(deliveryId)
			.orElseThrow(() -> new IllegalArgumentException("배송을 찾을 수 없습니다."));

		return new DeliveryResponse(
			delivery.getId(),
			delivery.getOrderId(),
			delivery.getStatus()
		);
	}



}
