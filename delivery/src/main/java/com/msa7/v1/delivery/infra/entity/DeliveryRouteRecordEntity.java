package com.msa7.v1.delivery.infra.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import org.hibernate.annotations.SQLDelete;

import com.msa7.v1.delivery.domain.vo.RouteStatus;
import com.msa7.v1.delivery.infra.persist.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "p_delivery_route_record")
@SQLDelete(sql = "UPDATE p_delivery_route_record SET deleted_at = NOW() WHERE id = ?")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DeliveryRouteRecordEntity extends BaseEntity {

	@Id
	@Column(name = "id")
	private UUID id;

	@Setter
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "delivery_id")
	private DeliveryEntity delivery; // 같은 애그리거트 참조

	@Column(name = "sequence")
	private Integer sequence;

	@Column(name = "start_hub_id")
	private UUID startHubId;

	@Column(name = "end_hub_id")
	private UUID endHubId;

	@Column(name = "estimated_distance")
	private Long estimatedDistance;

	@Column(name = "estimated_time")
	private Long estimatedTime;

	@Column(name = "actual_distance")
	private Long actualDistance;

	@Column(name = "actual_time")
	private Long actualTime;

	@Enumerated(EnumType.STRING)
	@Column(name = "status")
	private RouteStatus status;

	@Column(name = "delivery_manager_id")
	private UUID deliveryManagerId;

	@Column(name = "destination_address")
	private String destinationAddress;


	//  1. toEntity에서 사용하기 위한 Builder 추가
	@Builder
	public DeliveryRouteRecordEntity(UUID id, Integer sequence, UUID startHubId, UUID endHubId,
		Long estimatedDistance, Long estimatedTime,
		Long actualDistance, Long actualTime,
		RouteStatus status, UUID deliveryManagerId,
		LocalDateTime deletedAt, String deletedBy,
		String destinationAddress
		) {
		this.id = id;
		this.sequence = sequence;
		this.startHubId = startHubId;
		this.endHubId = endHubId;
		this.estimatedDistance = estimatedDistance;
		this.estimatedTime = estimatedTime;
		this.actualDistance = actualDistance;
		this.actualTime = actualTime;
		this.status = status;
		this.destinationAddress = destinationAddress;

		this.deliveryManagerId = deliveryManagerId;
		setDeletedInfo(deletedAt, deletedBy);
	}


}
