package com.msa7.v1.delivery.app;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;

import com.msa7.v1.delivery.domain.aggregateManager.DeliveryManager;
import com.msa7.v1.delivery.domain.repo.DeliveryManagerRepo;
import com.msa7.v1.delivery.domain.vo.ManagerType;
import com.msa7.v1.delivery.infra.feign.HubClient;
import com.msa7.v1.delivery.presentation.dto.CreateManagerRequest;

@Service
@RequiredArgsConstructor
public class DeliveryManagerService {
	private final DeliveryManagerRepo managerRepo;
	private final HubClient hubClient;

	@Transactional
	public UUID createDeliveryManager(CreateManagerRequest req) {
		// 1. 업체 배송 담당자일 경우, 소속 허브 존재 여부 검증
		if (req.type() == ManagerType.COMPANY_STAFF) {
			boolean exists = hubClient.checkHubExists(req.hubId());
			if (!exists) throw new IllegalArgumentException("존재하지 않는 허브입니다.");
		}

		// 2. 가장 마지막 배송 순번 조회
		Integer lastSeq = managerRepo.findMaxSequence().orElse(-1);

		// 3. 도메인 생성 및 새 순번 할당 (삭제된 순번 재배열 안함)
		DeliveryManager manager = DeliveryManager.create(
			req.userId(), req.hubId(), req.slackId(), req.type(), lastSeq + 1
		);

		return managerRepo.save(manager).getId();
	}

	@Transactional
	public void updateDeliveryManager(UUID id, UUID newHubId, ManagerType type) {
		DeliveryManager manager = managerRepo.findById(id)
			.orElseThrow(() -> new IllegalArgumentException("배송 담당자를 찾을 수 없습니다."));

		if (newHubId != null && !newHubId.equals(manager.getHubId())) {
			boolean exists = hubClient.checkHubExists(newHubId);
			if (!exists) {
				throw new IllegalArgumentException("존재하지 않는 허브 ID 입니다.");
			}
		}

		manager.updateInfo(newHubId, type);
		managerRepo.save(manager);
	}


	@Transactional
	public void deleteDeliveryManager(UUID managerId, String deletedBy) {
		DeliveryManager manager = managerRepo.findById(managerId)
			.orElseThrow(() -> new IllegalArgumentException("담당자를 찾을 수 없습니다."));

		manager.delete(deletedBy);
		managerRepo.save(manager);
	}


}
