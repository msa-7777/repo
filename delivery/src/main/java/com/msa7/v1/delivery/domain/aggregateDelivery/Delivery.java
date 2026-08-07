package com.msa7.v1.delivery.domain.aggregateDelivery;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.msa7.v1.delivery.domain.vo.DeliveryStatus;
import com.msa7.v1.delivery.domain.vo.DestinationAddr;

import lombok.Builder;
import lombok.Getter;

@Getter
public class Delivery {
	private final UUID id;

	private final UUID orderId;
	private final UUID receiverId;

	private DeliveryStatus status;
	private final DestinationAddr destinationAddr;

	private final List<DeliveryRouteRecord> routes = new ArrayList<>();

	@Builder
	public Delivery(UUID id, UUID orderId, UUID receiverId, DeliveryStatus status, String destinationAddr) {
		this.id = id;
		this.orderId = orderId;
		this.receiverId = receiverId;
		this.status = status;
		this.destinationAddr = new DestinationAddr(destinationAddr);
	}

	// 최초 배송 생성
	public static Delivery create(UUID orderId, UUID receiverId , String destinationAddr) {
		return Delivery.builder()
			.id(UUID.randomUUID())
			.orderId(orderId)
			.receiverId(receiverId)
			.status(DeliveryStatus.HUB_WAITING)
			.destinationAddr(destinationAddr)
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
	public void updateStatus(DeliveryStatus newStatus, UUID currentHubId) {
		if (this.status == DeliveryStatus.COMPANY_COMPLETED) {
			throw new IllegalStateException("이미 완료된 배송은 상태를 변경할 수 없습니다.");
		}
		// 상태 전이 규칙 검증 등 추가 가능
		this.status = newStatus;
	}


}
