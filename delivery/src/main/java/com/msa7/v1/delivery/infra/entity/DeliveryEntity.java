package com.msa7.v1.delivery.infra.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.hibernate.annotations.SQLDelete;

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
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "p_delivery")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SQLDelete(sql = "UPDATE p_delivery SET deleted_at = NOW() WHERE id = ?")
public class DeliveryEntity extends BaseEntity {
	@Id
	@Column(name = "id")
	private UUID id;

	@Column(name = "order_id", nullable = false)
	private UUID orderId;

	@Enumerated(EnumType.STRING)
	@Column(name = "status", nullable = false)
	private DeliveryStatus status;

	@Column(name = "start_hub_id")
	private UUID startHubId;

	@Column(name = "end_hub_id")
	private UUID endHubId;

	@Column(name = "destination_address", nullable = false)
	private String destinationAddress;

	@Column(name = "receiver_name")
	private String receiverName;

	@Column(name = "receiver_slack_id")
	private UUID receiverSlackId;

	@Column(name = "company_delivery_manager_id")
	private UUID companyDeliveryManagerId;

	// 애그리거트 내부 엔티티는 물리/논리적 생명주기를 같이하므로 양방향 매핑(Cascade) 설정
	@OneToMany(mappedBy = "delivery", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<DeliveryRouteRecordEntity> routes = new ArrayList<>();

	@Builder
	public DeliveryEntity(UUID id, UUID orderId, DeliveryStatus status, UUID startHubId, UUID endHubId,
		String destinationAddress, String receiverName, UUID receiverSlackId,
		UUID companyDeliveryManagerId) {
		this.id = id;
		this.orderId = orderId;
		this.status = status;
		this.startHubId = startHubId;
		this.endHubId = endHubId;
		this.destinationAddress = destinationAddress;
		this.receiverName = receiverName;
		this.receiverSlackId = receiverSlackId;
		this.companyDeliveryManagerId = companyDeliveryManagerId;
	}

	public void addRoute(DeliveryRouteRecordEntity routeEntity) {
		this.routes.add(routeEntity);
		routeEntity.setDelivery(this);
	}

	public void setRoutes(List<DeliveryRouteRecordEntity> routes) {
		this.routes.clear();
		for (DeliveryRouteRecordEntity route : routes) {
			this.routes.add(route);
			route.setDelivery(this);
		}
	}

	public void delete(UUID deletedBy){
		LocalDateTime now = LocalDateTime.now();
		this.setDeletedInfo(now, deletedBy);
		for(DeliveryRouteRecordEntity route : this.routes){
			route.delete(deletedBy);
		}
	}



}
