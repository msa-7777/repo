package com.msa7.v1.delivery.domain.aggregateDelivery;

import java.util.UUID;

import com.msa7.v1.delivery.domain.vo.RouteMetrics;
import com.msa7.v1.delivery.domain.vo.RouteStatus;

import lombok.Builder;
import lombok.Getter;

@Getter
public class DeliveryRouteRecord {
	private final UUID id;
	private final Integer sequence;
	private final UUID startHubId;
	private final UUID endHubId;

	private final RouteMetrics metrics;
	private RouteStatus status;
	private final UUID deliveryManagerId;

	@Builder
	public DeliveryRouteRecord(UUID id, Integer sequence, UUID startHubId, UUID endHubId, RouteMetrics metrics, RouteStatus status, UUID deliveryManagerId) {
		this.id = id;
		this.sequence = sequence;
		this.startHubId = startHubId;
		this.endHubId = endHubId;
		this.metrics = metrics;
		this.status = status;
		this.deliveryManagerId = deliveryManagerId;
	}

	public static DeliveryRouteRecord create(Integer sequence, UUID startHubId, UUID endHubId, Long distance, Long time, UUID deliveryManagerId) {
		return DeliveryRouteRecord.builder()
			.id(UUID.randomUUID())
			.sequence(sequence)
			.startHubId(startHubId)
			.endHubId(endHubId)
			.metrics(new RouteMetrics(distance, time))
			.status(RouteStatus.WAITING)
			.deliveryManagerId(deliveryManagerId)
			.build();
	}

	public void updateStatus(RouteStatus newStatus) {
		this.status = newStatus;
	}


}
