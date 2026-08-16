package com.msa7.v1.delivery.infra.repo;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Repository;

import com.msa7.v1.delivery.domain.aggregateManager.DeliveryManager;
import com.msa7.v1.delivery.domain.repo.DeliveryManagerRepo;
import com.msa7.v1.delivery.domain.vo.ManagerType;
import com.msa7.v1.delivery.infra.entity.DeliveryManagerEntity;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class DeliveryManagerRepositoryImpl implements DeliveryManagerRepo {
	private final JpaDeliveryManagerRepository jpaRepo;

	@Override
	public DeliveryManager save(DeliveryManager manager) {
		// 데이터가 존재하면 삭제 정보 유지 위한 코드
		DeliveryManagerEntity entity = jpaRepo.findById(manager.getId()).orElse(toEntity(manager));
		DeliveryManagerEntity savedEntity = jpaRepo.save(entity);
		return toDomain(savedEntity);
	}

	@Override
	public Optional<DeliveryManager> findById(UUID id) {
		return jpaRepo.findById(id).map(this::toDomain);
	}

	@Override
	public Optional<Integer> findMaxSequence() {
		return jpaRepo.findMaxSequence();
	}

	@Override
	public Optional<DeliveryManager> findNextAvailableManager(UUID hubId, ManagerType type, Integer lastAssignedSeq) {
		return jpaRepo.findFirstByHubIdAndTypeAndAssignmentSeqGreaterThanOrderByAssignmentSeqAsc(hubId, type,
				lastAssignedSeq)
			.map(this::toDomain);
	}

	private DeliveryManagerEntity toEntity(DeliveryManager domain) {
		return new DeliveryManagerEntity(
			domain.getId(),
			domain.getHubId(),
			domain.getSlackId(),
			domain.getType(),
			domain.getAssignmentSeq()
		);
	}
	private DeliveryManager toDomain(DeliveryManagerEntity entity) {
		return DeliveryManager.builder()
			.id(entity.getId())
			.hubId(entity.getHubId())
			.slackId(entity.getSlackId())
			.type(entity.getType())
			.assignmentSeq(entity.getAssignmentSeq())
			.build();
	}

}
