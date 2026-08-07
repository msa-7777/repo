package com.msa7.v1.delivery.domain.repo;

import java.util.Optional;
import java.util.UUID;

import com.msa7.v1.delivery.domain.aggregateManager.DeliveryManager;

public interface DeliveryManagerRepo {
	Optional<DeliveryManager> findById(UUID id);
}
