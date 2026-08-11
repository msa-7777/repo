package com.msa7.v1.delivery.domain.vo;

public record ActualRouteMetrics(Long actualDistance, Long actualTime) {
	public ActualRouteMetrics {
		if (actualDistance < 0 || actualTime < 0) {
			throw new IllegalArgumentException("실제 거리와 시간은 0 이상이어야 합니다.");
		}
	}
}
