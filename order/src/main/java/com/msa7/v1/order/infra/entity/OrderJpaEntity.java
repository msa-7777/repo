package com.msa7.v1.order.infra.entity;

import java.util.UUID;

import com.msa7.v1.order.domain.aggregate.Order;
import com.msa7.v1.order.domain.vo.OrderStatus;
import com.msa7.v1.order.global.common.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

// db 테이블 매핑
@Entity
@Table(name = "p_orders")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderJpaEntity extends BaseEntity {
	@Id
	private UUID id;

	@Column(name = "receiver_company_id")
	private UUID receiverCompanyId;

	@Column(name = "product_id")
	private UUID productId;

	@Column(name = "quantity")
	private Integer quantity;

	@Enumerated(EnumType.STRING)
	@Column(name = "status")
	private OrderStatus status;

	@Column(name = "request_notes")
	private String requestNotes;

	// Domain -> Entity
	public static OrderJpaEntity from(Order order) {
		OrderJpaEntity entity = new OrderJpaEntity();
		entity.id = order.getId();
		entity.receiverCompanyId = order.getReceiverCompanyId();
		entity.productId = order.getProductId();
		// VO 파싱해서 원시값만 entity에 세팅
		entity.quantity = order.getQuantity().value();
		entity.requestNotes = order.getRequestNotes().contents();
		entity.status = order.getStatus();
		return entity;
	}

	// Entity -> Domain
	public Order toDomain() {
		return new Order(
			this.id,
			this.receiverCompanyId,
			this.productId,
			this.quantity,
			this.status,
			this.requestNotes
		);
	}
}
