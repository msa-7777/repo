package com.msa7.v1.delivery.app;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.msa7.v1.delivery.domain.aggregateDelivery.Delivery;
import com.msa7.v1.delivery.domain.aggregateDelivery.DeliveryRouteRecord;
import com.msa7.v1.delivery.domain.repo.DeliveryRepo;
import com.msa7.v1.delivery.domain.vo.DeliveryStatus;
import com.msa7.v1.delivery.domain.vo.RouteStatus;
import com.msa7.v1.delivery.infra.feign.HubClient;
import com.msa7.v1.delivery.infra.feign.UserClient;
import com.msa7.v1.delivery.infra.outobx.DeliveryOutboxEvent;
import com.msa7.v1.delivery.infra.outobx.DeliveryOutboxEventRepo;
import com.msa7.v1.delivery.infra.repo.JpaDeliveryRouteRecordRepository;
import com.msa7.v1.delivery.presentation.dto.HubRouteResponse;
import com.msa7.v1.delivery.presentation.dto.payload.DeliveryFailedEvent;
import com.msa7.v1.delivery.presentation.dto.payload.DeliveryResponse;
import com.msa7.v1.delivery.presentation.dto.payload.DeliveryRouteResponse;
import com.msa7.v1.delivery.presentation.dto.payload.OrderCreatedEvent;
import com.msa7.v1.delivery.presentation.internal.dto.DeliveryRouteInfoResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeliveryService {

	private final DeliveryRepo deliveryRepo;
	private final UserClient userClient;
	private final HubClient hubClient;
	private final JpaDeliveryRouteRecordRepository routeRepo;
	private final DeliveryOutboxEventRepo outboxEventRepo;
	private final ObjectMapper objectMapper;

	// @Transactional
	// public UUID createDelivery(UUID orderId, UUID startHubId, UUID endHubId, String destinationAddress,
	// 	String receiverName, UUID receiverSlackId) {
	//
	// 	// 1. 업체 배송 담당자 할당
	// 	UUID companyManagerId = userClient.getNextDeliveryManagerId(endHubId);
	//
	// 	// 2. 도메인 객체 생성
	// 	Delivery delivery = Delivery.create(orderId, startHubId, endHubId, receiverName,destinationAddress,receiverSlackId, companyManagerId);
	//
	// 	// 3. 배송 경로 기록 일괄 생성 (최초 생성 시 전체 경로 세팅)
	// 	HubRouteResponse hubRoute = hubClient.getRouteInfo(startHubId, endHubId);
	// 	List<HubRouteResponse> hubRoutes = List.of(hubRoute);
	// 	List<DeliveryRouteRecord> routes = new ArrayList<>();
	//
	// 	int sequence = 0;
	//
	// 	for (HubRouteResponse res : hubRoutes) {
	// 		// 각 구간마다 담당할 허브 배송 담당자를 순차 할당 (UserClient 활용)
	// 		UUID hubDeliveryManagerId = userClient.getNextDeliveryManagerId(res.startHubId());
	//
	// 		DeliveryRouteRecord route = DeliveryRouteRecord.create(
	// 			sequence++,
	// 			res.startHubId(),
	// 			res.endHubId(),
	// 			res.estimatedDistance(),
	// 			res.estimatedTime(),
	// 			hubDeliveryManagerId
	// 		);
	// 		routes.add(route);
	// 	}
	//
	// 	// 4. 경로 할당 및 저장
	// 	delivery.assignRoutes(routes);
	// 	deliveryRepo.save(delivery);
	//
	// 	return delivery.getId();
	// }

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

	@Transactional
	public void createDeliveryFromOrder(OrderCreatedEvent event) {
		try {
			// 업체 배송 담당자 할당
			List<UUID> companyManagers = userClient.getDeliveryManagersByHubId(event.endHubId());

			if (companyManagers == null || companyManagers.isEmpty()) {
				throw new IllegalStateException("도착지 허브에 할당 가능한 배송 담당자가 없습니다. hubId: " + event.endHubId());
			}
			UUID companyManagerId = companyManagers.get(ThreadLocalRandom.current().nextInt(companyManagers.size()));

			// 도메인 객체 생성 (성공 이벤트 내부에 적재)
			Delivery delivery = Delivery.createFromOrder(
				event.orderId(),
				event.startHubId(),
				event.endHubId(),
				event.destinationAddress(),
				event.receiverSlackId(),
				companyManagerId
			);

			// 3. 배송 경로 조회 및 기록 일괄 생성
			HubRouteResponse hubRoute = hubClient.getRouteInfo(event.startHubId(), event.endHubId());
			List<HubRouteResponse> hubRoutes = List.of(hubRoute);
			List<DeliveryRouteRecord> routes = new ArrayList<>();

			int sequence = 0;
			for (HubRouteResponse res : hubRoutes) {
				List<UUID> hubManagers = userClient.getDeliveryManagersByHubId(res.startHubId());

				if (hubManagers == null || hubManagers.isEmpty()) {
					throw new IllegalStateException("출발지 허브에 할당 가능한 배송 담당자가 없습니다. hubId: " + res.startHubId());
				}

				UUID hubDeliveryManagerId = hubManagers.get(ThreadLocalRandom.current().nextInt(hubManagers.size()));

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

			// 경로 할당
			delivery.assignRoutes(routes);

			// 저장 시 RepositoryImpl 내부 로직을 타고 성공 이벤트가 Outbox 테이블로 자동 기록됨
			deliveryRepo.save(delivery);

		} catch (Exception e) {
			// SAGA 실패 보상: 예외를 던져 롤백하지 않고, 실패 내역을 즉시 Outbox에 밀어넣음.
			// (그래야 Order 서비스가 실패 메시지를 받아 주문을 취소시킬 수 있음)
			try {
				DeliveryFailedEvent failedEvent = new DeliveryFailedEvent(event.orderId(), "경로 조회 또는 생성 실패: " + e.getMessage());
				DeliveryOutboxEvent outboxEvent = new DeliveryOutboxEvent(
					"Delivery",
					event.orderId().toString(),
					"DeliveryFailedEvent",
					objectMapper.writeValueAsString(failedEvent)
				);
				outboxEventRepo.save(outboxEvent);
			} catch (Exception parseException) {
				log.error("Outbox 실패 이벤트 직렬화 중 오류 발생: orderId={}", event.orderId(), parseException);
			}
		}
	}

	@Transactional(readOnly = true)
	public boolean existsActiveDeliveryByHubId(UUID hubId) {
		return routeRepo.existsActiveRouteByHubId(hubId);
	}

	@Transactional(readOnly = true)
	public DeliveryRouteInfoResponse getDeliveryInfoByOrderId(UUID orderId) {
		Delivery delivery = deliveryRepo.findByOrderId(orderId).orElseThrow(
			() -> new IllegalArgumentException("해당 주문 배송 정보 x")
		);
		List<DeliveryRouteInfoResponse.DeliveryRouteInfoDetails> routes =
			delivery.getRoutes().stream().map(
				x -> new
					DeliveryRouteInfoResponse.DeliveryRouteInfoDetails(
						x.getSequence(),
						x.getDeliveryManagerId(),
						x.getStatus().name()
				)
			).toList();
		return new DeliveryRouteInfoResponse(
			delivery.getId(),
			delivery.getStartHubId(),
			delivery.getEndHubId(),
			delivery.getDestinationAddress().address(),
			delivery.getCompanyDeliveryManagerId(),
			routes
		);
	}

}
