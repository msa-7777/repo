package com.msa7.v1.delivery.domain.repo;

import java.util.Optional;
import java.util.UUID;

import com.msa7.v1.delivery.domain.aggregateDelivery.Delivery;
import com.msa7.v1.delivery.domain.aggregateManager.DeliveryManager;
import com.msa7.v1.delivery.domain.vo.ManagerType;

public interface DeliveryManagerRepo {
	DeliveryManager save(DeliveryManager manager);
	Optional<Integer> findMaxSequence();
	Optional<DeliveryManager> findNextAvailableManager(UUID hubId, ManagerType type, Integer lastAssignedSeq);
	Optional<DeliveryManager> findById(UUID id);
}
