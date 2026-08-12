package com.msa7.v1.delivery.presentation.dto;

import com.msa7.v1.delivery.domain.vo.DeliveryStatus;
import com.msa7.v1.delivery.domain.vo.RouteStatus;

public record RouteStatusUpdateRequest(
	RouteStatus status
) {
}
