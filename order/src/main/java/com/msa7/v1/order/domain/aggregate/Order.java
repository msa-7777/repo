package com.msa7.v1.order.domain.aggregate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import com.msa7.v1.order.domain.vo.OrderStatus;
import com.msa7.v1.order.domain.vo.Quantity;
import com.msa7.v1.order.domain.vo.RequestNotes;

import lombok.Getter;

@Getter
public class Order {
	private final UUID id;

	// 외부 도메인 참조는 오직 ID로만
	private final UUID receiverCompanyId;
	private final UUID productId;

	// Value Obj
	private final Quantity quantity;
	private  OrderStatus status;
	private final RequestNotes requestNotes;

	// 이벤트 저장을 위한 임시 컬랙션
	private List<Object> domainEvents = new ArrayList<>();

	// DB 복원자
	public Order(UUID id, UUID receiverCompanyId, UUID productId, Integer quantity, OrderStatus status, String requestNotes) {
		this.id = id;
		this.receiverCompanyId = receiverCompanyId;
		this.productId = productId;
		this.quantity = new Quantity(quantity);
		this.status = status;
		this.requestNotes = new RequestNotes(requestNotes);

	}
	// 신규 생성자
	public static Order create(UUID receiverCompanyId, UUID productId, Integer quantity, String requestNotes) {
		return new Order(
			UUID.randomUUID(),
			receiverCompanyId,
			productId,
			quantity,
			OrderStatus.PENDING,
			requestNotes
			// domainEvents.add(new OrderCreate)
		);
	}

	public void cancel(UUID requestId) {
		if (this.status == OrderStatus.DELIVERING || this.status == OrderStatus.CANCELED) {
			throw new IllegalStateException("이미 배송중 이거나 완료된 주문은 취소가 불가 합니다");
		}
		this.status = OrderStatus.CANCELED;

		// this.domainEvents.add(new OrderCanceldEvent(this.id, requestId));
	}

	// callBack메서드 1
	public void startDelivery() {
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

	// 이벤트 방출 용
	public List<Object> getDomainEvents() {return Collections.unmodifiableList(domainEvents);}
	public void clearEvents() { this.domainEvents.clear();}


}
