package com.msa7.v1.order.infra.repo;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.msa7.v1.order.infra.entity.OrderOutboxEntity;

public interface OrderOutboxRepository extends JpaRepository<OrderOutboxEntity, UUID> {
	List<OrderOutboxEntity> findByPublishedFalse();
}
