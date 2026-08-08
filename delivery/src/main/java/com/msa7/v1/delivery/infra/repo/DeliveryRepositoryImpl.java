package com.msa7.v1.delivery.infra.repo;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Repository;

import com.msa7.v1.delivery.domain.aggregateDelivery.Delivery;
import com.msa7.v1.delivery.domain.repo.DeliveryRepo;
import com.msa7.v1.delivery.infra.entity.DeliverJpaEntity;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class DeliveryRepositoryImpl implements DeliveryRepo {

	private final JpaDeliveryRepository jpaDeliveryRepository;

	@Override
	public Delivery save(Delivery delivery) {
		// Domain Model -> JPA Entity 변환
		DeliverJpaEntity entity = toEntity(delivery);
		DeliverJpaEntity savedEntity = jpaDeliveryRepository.save(entity);

		// JPA Entity -> Domain Model 변환 후 반환
		return toDomain(savedEntity);
	}

	@Override
	public Optional<Delivery> findById(UUID id) {
		return jpaDeliveryRepository.findById(id)
			.map(this::toDomain);
	}

	// Mapper 로직 (MapStruct 등을 활용할 수 있음)
	private DeliverJpaEntity toEntity(Delivery domain) {
		DeliverJpaEntity entity = new DeliverJpaEntity(
			domain.getId(),
			domain.getOrderId(),
			domain.getReceiverId(),
			domain.getStatus(),
			domain.getDestinationAddr().address()
		);
		// 하위 엔티티(경로) 매핑 로직 추가 가능
		return entity;
	}

	private Delivery toDomain(DeliverJpaEntity entity) {
		return Delivery.builder()
			.id(entity.getId())
			.orderId(entity.getOrderId())
			.receiverId(entity.getReceiverId())
			.status(entity.getStatus())
			.destinationAddr(entity.getDestinationAddress())
			.build();
	}
}
