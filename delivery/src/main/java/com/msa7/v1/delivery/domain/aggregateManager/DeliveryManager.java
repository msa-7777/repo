package com.msa7.v1.delivery.domain.aggregateManager;

import java.util.UUID;

import com.msa7.v1.delivery.domain.vo.ManagerType;

import lombok.Builder;

public class DeliveryManager {

	private final UUID id;
	private final UUID hubId;

	private final ManagerType type;
	private final Integer assignmentSeq;

	@Builder
	public DeliveryManager(UUID id, UUID hubId, ManagerType type, Integer assignmentSeq) {
		this.id = id;
		this.hubId = hubId;
		this.type = type;
		this.assignmentSeq = assignmentSeq;
	}
}
