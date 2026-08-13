package com.msa7.v1.delivery.domain.aggregateDelivery;

import java.time.LocalDateTime;
import java.util.UUID;

import com.msa7.v1.delivery.domain.vo.ActualRouteMetrics;
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
	private final String destinationAddress;

	private final RouteMetrics metrics;
	private  ActualRouteMetrics actualMetrics;
	private RouteStatus status;
	private final UUID deliveryManagerId;

	private LocalDateTime deletedAt;
	private String deletedBy;

	@Builder
	public DeliveryRouteRecord(UUID id, Integer sequence, UUID startHubId, UUID endHubId,
		RouteMetrics metrics, ActualRouteMetrics actualMetrics,
		RouteStatus status, UUID deliveryManagerId,
		LocalDateTime deletedAt, String deletedBy, String destinationAddress
		) {
		this.id = id;
		this.sequence = sequence;
		this.startHubId = startHubId;
		this.endHubId = endHubId;
		this.metrics = metrics;
		this.actualMetrics = actualMetrics;
		this.status = status;
		this.deliveryManagerId = deliveryManagerId;
		this.deletedAt = deletedAt;
		this.deletedBy = deletedBy;
		this.destinationAddress = destinationAddress;
	}

	public static DeliveryRouteRecord create(Integer sequence, UUID startHubId, UUID endHubId,
		String destinationAddress, Long estimatedDistance, Long estimatedTime, UUID deliveryManagerId) {
		return DeliveryRouteRecord.builder()
			.id(UUID.randomUUID())
			.sequence(sequence)
			.startHubId(startHubId)
			.endHubId(endHubId)
			.metrics(new RouteMetrics(estimatedDistance, estimatedTime))
			.actualMetrics(new ActualRouteMetrics(0L, 0L))
			.status(RouteStatus.WAITING)
			.deliveryManagerId(deliveryManagerId)
			.destinationAddress(destinationAddress)
			.build();
	}

	public void updateStatus(RouteStatus newStatus) {
		this.status = newStatus;
	}



	public void updateActualMetrics(Long actualDistance, Long actualTime) {
		this.actualMetrics = new ActualRouteMetrics(actualDistance, actualTime);
	}

	public void delete(String deletedBy) {
		this.deletedAt = LocalDateTime.now();
		this.deletedBy = deletedBy;
	}
}


