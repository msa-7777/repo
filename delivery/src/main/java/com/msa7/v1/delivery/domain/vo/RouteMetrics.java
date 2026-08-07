package com.msa7.v1.delivery.domain.vo;

public record RouteMetrics(Long estimantedDistance, Long estimatedTime) {
	public RouteMetrics{
		if(estimantedDistance<0|| estimatedTime<0){
			throw new IllegalArgumentException("거리와 시간은 0보다 커야합니다.");
		}
	}

}
