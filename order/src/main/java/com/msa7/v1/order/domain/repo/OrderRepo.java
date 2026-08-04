package com.msa7.v1.order.domain.repo;

import java.util.UUID;

import com.msa7.v1.order.domain.aggregate.Order;

public interface OrderRepo {
	Order save(Order order);
	Order findById(UUID id);
}
