package com.msa7.v1.delivery.domain.aggregateManager;

import java.time.LocalDateTime;
import java.util.UUID;

import com.msa7.v1.delivery.domain.vo.ManagerType;

import lombok.Builder;
import lombok.Getter;

@Getter
public class DeliveryManager {

	private final UUID id;
	private  UUID hubId;
	private final UUID slackId;

	private  ManagerType type;
	private Integer assignmentSeq;

	private LocalDateTime deletedAt;
	private String deletedBy;

	@Builder
	public DeliveryManager(UUID id, UUID hubId, UUID slackId,
		ManagerType type, Integer assignmentSeq,
		LocalDateTime deletedAt, String deletedBy) {
		this.id = id;
		this.hubId = hubId;
		this.slackId = slackId;
		this.type = type;
		this.assignmentSeq = assignmentSeq;
		this.deletedAt = deletedAt;
		this.deletedBy = deletedBy;
	}

	public static DeliveryManager create(UUID userId, UUID hubId, UUID slackId, ManagerType type, Integer lastAssignmentSeq) {
		return DeliveryManager.builder()
			.id(userId)
			.hubId(hubId)
			.slackId(slackId)
			.type(type)
			.assignmentSeq(lastAssignmentSeq != null ? lastAssignmentSeq + 1 : 0) // 새로운 담당자는 가장 마지막 순번으로 설정
			.build();
	}

	public void updateInfo(UUID hubId, ManagerType type) {
		this.hubId = hubId;
		this.type = type;
	}

	public void delete(String deletedBy) {
		this.deletedAt = LocalDateTime.now();
		this.deletedBy = deletedBy;
	}

}
