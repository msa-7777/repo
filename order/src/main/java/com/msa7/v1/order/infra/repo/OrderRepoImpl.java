package com.msa7.v1.order.infra.repo;

import java.util.Optional;
import java.util.UUID;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Repository;

import com.msa7.v1.order.domain.aggregate.Order;
import com.msa7.v1.order.domain.repo.OrderRepo;
import com.msa7.v1.order.infra.entity.OrderJpaEntity;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class OrderRepoImpl implements OrderRepo {
	private final OrderJpaRepo jpaRepo;
	private final ApplicationEventPublisher eventPublisher;

	@Override
	public Order save(Order order) {
		// 도메인 -> jpaEntity 변환
		OrderJpaEntity entity = OrderJpaEntity.from(order);
		// DB에 저장
		OrderJpaEntity savedEntity = jpaRepo.save(entity);

		//도메인 이벤트
		// order.getDomainEvents().forEach(eventPublisher::publishEvent);
		// order.clearEvents();
		// jpaEntity -> 도메인 변환후 반환
		return savedEntity.toDomain();
	}

	@Override
	public Optional<Order> findById(UUID id) {
		return jpaRepo.findById(id)
			.map(OrderJpaEntity::toDomain);
	}

	@Override
	public void delete(Order order) {
		jpaRepo.deleteById(order.getId());
	}

}
