package com.msa7.v1.order.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import com.msa7.v1.order.domain.vo.OrderStatus;
import com.msa7.v1.order.domain.vo.Quantity;
import com.msa7.v1.order.domain.vo.RequestNotes;

import lombok.Getter;

@Getter
public class Order {
	private UUID id;

	// 외부 도메인 참조는 오직 ID로만
	private UUID receiverCompanyId;
	private UUID productId;

	// Value Obj
	private Quantity quantity;
	private OrderStatus status;
	private RequestNotes requestNotes;

	// 이벤트 저장을 위한 임시 컬랙션
	private List<Object> domainEvents = new ArrayList<>();

	public Order(UUID receiverCompanyId, UUID productId, Integer quantity, OrderStatus status, String requestNotes) {
		this.id = UUID.randomUUID();
		this.receiverCompanyId = receiverCompanyId;
		this.productId = productId;
		this.quantity = new Quantity(quantity);
		this.status = OrderStatus.PENDING;
		this.requestNotes = new RequestNotes(requestNotes);

		// this.domainEvents.add(new OrderCreate)
	}

	public void cancel(UUID requestId) {
		if (this.status == OrderStatus.DELIVERING || this.status == OrderStatus.CANCELED) {
			throw new IllegalStateException("이미 배송중 이거나 완료된 주문은 취소가 불가 합니다");
		}
		this.status = OrderStatus.CANCELED;

		// this.domainEvents.add(new OrderCanceldEvent(this.id, requestId));
	}

	// callBack메서드 1
	public void setDelivery() {
		if (this.status == OrderStatus.CANCELED) {
			throw new IllegalStateException("취소된 주문은 배송 불가 합니다");
		}
		this.status = OrderStatus.DELIVERING;
	}
	// callBack메서드 2
	public void completeOrder(UUID requestId) {
		if (this.status != OrderStatus.DELIVERING) {
			throw new IllegalStateException("배송 중 인 주문만 완료 가능 합니다");
		}
		this.status = OrderStatus.COMPLETED;
	}


}
