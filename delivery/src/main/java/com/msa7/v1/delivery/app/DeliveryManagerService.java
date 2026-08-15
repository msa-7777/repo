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
	public UUID createDeliveryManager(UUID userId, UUID slackId, UUID hubId, ManagerType type) {
		if (hubId != null) {
			boolean exists = hubClient.checkHubExists(hubId);
			if (!exists) {
				throw new IllegalArgumentException("존재하지 않는 허브 ID 입니다.");
			}
		}
		int nextSeq = managerRepo.findMaxSequence().orElse(0) + 1;

		DeliveryManager manager = DeliveryManager.create(userId, slackId, hubId, type, nextSeq);
		managerRepo.save(manager);

		return manager.getId();
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
	public void deleteDeliveryManager(UUID managerId, UUID deletedBy) { // String -> UUID 변경
		DeliveryManager manager = managerRepo.findById(managerId)
			.orElseThrow(() -> new IllegalArgumentException("담당자를 찾을 수 없습니다."));

		manager.delete(deletedBy);
		managerRepo.save(manager);
	}


}