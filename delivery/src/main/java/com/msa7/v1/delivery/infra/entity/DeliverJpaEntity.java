package com.msa7.v1.delivery.infra.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.msa7.v1.delivery.domain.vo.DeliveryStatus;
import com.msa7.v1.delivery.infra.persist.BaseEntity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "p_delivery")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DeliverJpaEntity extends BaseEntity {
	@Id
	@Column(name = "id")
	private UUID id;

	// MSA 타 도메인 참조 (물리적 FK X, 논리적 UUID)
	@Column(name = "order_id", nullable = false)
	private UUID orderId;

	@Column(name = "receiver_id", nullable = false)
	private UUID receiverId;

	@Enumerated(EnumType.STRING)
	@Column(name = "status", nullable = false)
	private DeliveryStatus status;

	@Column(name = "destination_address", nullable = false)
	private String destinationAddress;

	// 애그리거트 내부 엔티티는 물리/논리적 생명주기를 같이하므로 양방향 매핑(Cascade) 설정
	@OneToMany(mappedBy = "delivery", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<DeliveryRouteRecordEntity> routes = new ArrayList<>();

	public DeliverJpaEntity(UUID id, UUID orderId, UUID receiverId, DeliveryStatus status, String destinationAddress) {
		this.id = id;
		this.orderId = orderId;
		this.receiverId = receiverId;
		this.status = status;
		this.destinationAddress = destinationAddress;
	}

	public void addRoute(DeliveryRouteRecordEntity routeEntity) {
		this.routes.add(routeEntity);
		routeEntity.setDelivery(this);
	}
}
