package com.msa7.v1.delivery.presentation.dto;

import java.util.UUID;

import com.msa7.v1.delivery.domain.vo.DeliveryStatus;

public record UpdateDeliveryStatusRequest(
	DeliveryStatus status,
	UUID currentHubId
) {}
