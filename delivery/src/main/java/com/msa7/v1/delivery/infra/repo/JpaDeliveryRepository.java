package com.msa7.v1.delivery.infra.repo;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.msa7.v1.delivery.infra.entity.DeliveryEntity;

public interface JpaDeliveryRepository extends JpaRepository<DeliveryEntity, UUID> {
	Optional<DeliveryEntity> findByOrderIdAndDeletedAtIsNull(UUID orderId);
}
