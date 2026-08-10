package com.msa7.v1.delivery.domain.aggregateDelivery;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.msa7.v1.delivery.domain.vo.DeliveryStatus;
import com.msa7.v1.delivery.domain.vo.DestinationAddress;

import lombok.Builder;
import lombok.Getter;

@Getter
public class Delivery {
	private final UUID id;
	private final UUID orderId;
	private final UUID startHubId;
	private final UUID endHubId;

	private final String receiverName;
	private final UUID receiverSlackId;

	private  UUID companyDeliveryManagerId;

	private DeliveryStatus status;
	private final DestinationAddress destinationAddress;

	// saga Event 버퍼
	private final List<DeliveryRouteRecord> routes = new ArrayList<>();

	private LocalDateTime deletedAt;
	private String deletedBy;

	@Builder
	public Delivery(UUID id, UUID orderId, DeliveryStatus status, UUID startHubId, UUID endHubId,
		String destinationAddress, String receiverName, UUID receiverSlackId, UUID companyDeliveryManagerId
	, LocalDateTime deletedAt, String deletedBy) {
		this.id = id;
		this.orderId = orderId;
		this.status = status;
		this.startHubId = startHubId;
		this.endHubId = endHubId;
		this.destinationAddress = new DestinationAddress(destinationAddress);
		this.receiverName = receiverName;
		this.receiverSlackId = receiverSlackId;
		this.companyDeliveryManagerId = companyDeliveryManagerId;
		this.deletedAt = deletedAt;
		this.deletedBy = receiverName;
	}

	// 최초 배송 생성
	public static Delivery create(UUID orderId, UUID startHubId, UUID endHubId,
		String destinationAddress, String receiverName, UUID receiverSlackId, UUID companyDeliveryManagerId) {
		return Delivery.builder()
			.id(UUID.randomUUID())
			.orderId(orderId)
			.status(DeliveryStatus.HUB_WAITING)
			.startHubId(startHubId)
			.endHubId(endHubId)
			.destinationAddress(destinationAddress)
			.receiverName(receiverName)
			.receiverSlackId(receiverSlackId)
			.companyDeliveryManagerId(companyDeliveryManagerId)
			.build();
	}


	// 전체 경로 최초 일괄 세팅
	public void assignRoutes(List<DeliveryRouteRecord> newRoutes) {
		if (!this.routes.isEmpty()) {
			throw new IllegalStateException("배송 경로는 이미 설정되어 있습니다.");
		}
		this.routes.addAll(newRoutes);
	}

	// 배송 상태 변경
	public void updateStatus(DeliveryStatus newStatus) {
		if (this.status == DeliveryStatus.COMPLETED) {
			throw new IllegalStateException("이미 완료된 배송은 상태를 변경할 수 없습니다.");
		}
		this.status = newStatus;
	}

	public void assignCompanyManager(UUID managerId) {
		this.companyDeliveryManagerId = managerId;
	}

	public void delete(String deletedBy) {
		this.deletedAt = LocalDateTime.now();
		this.deletedBy = deletedBy;
		for (DeliveryRouteRecord route : routes) {
			route.delete(deletedBy);
		}
	}
}

