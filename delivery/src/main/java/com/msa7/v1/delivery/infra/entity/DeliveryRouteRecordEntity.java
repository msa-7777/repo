package com.msa7.v1.delivery.infra.entity;

import java.util.UUID;

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
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "p_delivery_route_record")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DeliveryRouteRecordEntity extends BaseEntity {

	@Id
	@Column(name = "id")
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "delivery_id")
	private DeliverJpaEntity delivery; // 같은 애그리거트 참조

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

	@Enumerated(EnumType.STRING)
	@Column(name = "status")
	private RouteStatus status;

	@Column(name = "delivery_manager_id")
	private UUID deliveryManagerId;

	public void setDelivery(DeliverJpaEntity delivery) {
		this.delivery = delivery;
	}
}
