package com.msa7.v1.delivery.presentation.dto;

import java.util.UUID;

import com.msa7.v1.delivery.domain.vo.ManagerType;

public record CreateManagerRequest(
	UUID userId, UUID hubId, UUID slackId, ManagerType type
) {}
