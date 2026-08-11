package com.msa7.v1.delivery.domain.vo;

public record RouteMetrics(Long distance, Long time) {
	public RouteMetrics {
		if (distance != null && distance < 0) {
			throw new IllegalArgumentException("거리는 0 이상이어야 합니다.");
		}
		if (time != null && time < 0) {
			throw new IllegalArgumentException("시간은 0 이상이어야 합니다.");
		}
	}
}
