package com.msa7.v1.delivery.infra.outobx;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface DeliveryOutboxEventRepo extends JpaRepository<DeliveryOutboxEvent, UUID> {
	List<DeliveryOutboxEvent> findAllByPublishedFalse();
}
