package com.msa7.v1.order.infra.repo;

import java.util.Optional;
import java.util.UUID;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Repository;

import com.msa7.v1.order.domain.aggregate.Order;
import com.msa7.v1.order.domain.repo.OrderRepo;
import com.msa7.v1.order.infra.entity.OrderEntity;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class OrderRepoImpl implements OrderRepo {
	private final OrderJpaRepository jpaRepo;
	private final ApplicationEventPublisher eventPublisher;

	@Override
	public Order save(Order order) {
		// 도메인 -> jpaEntity 변환
		OrderEntity entity = OrderEntity.fromDomain(order);
		// DB에 저장
		jpaRepo.save(entity);

		// 애거리거트에 쌓인 도메인 이벤트를 스프링 컨텍스트로 발행 (outbox 트리거)
		order.getDomainEvents().forEach(eventPublisher::publishEvent);
		order.clearEvents();
		return order;
	}

	@Override
	public Optional<Order> findById(UUID id) {
		return jpaRepo.findById(id)
			.map(OrderEntity::toDomain);
	}
}
