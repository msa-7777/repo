package com.msa7.v1.delivery.domain.repo;

import java.util.Optional;
import java.util.UUID;

import com.msa7.v1.delivery.domain.aggregateDelivery.Delivery;

public interface DeliveryRepo {
	Delivery save(Delivery delivery);
	Optional<Delivery> findById(UUID id);
	// 검색조건에 따른 페이징 조회 생략
}
