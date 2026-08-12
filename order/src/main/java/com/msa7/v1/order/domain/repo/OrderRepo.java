package com.msa7.v1.order.domain.repo;

import java.util.Optional;
import java.util.UUID;

import com.msa7.v1.order.domain.aggregate.Order;

public interface OrderRepo {
	Order save(Order order);
	Optional<Order> findById(UUID id);
}
