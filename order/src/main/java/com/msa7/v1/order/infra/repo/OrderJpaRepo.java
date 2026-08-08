package com.msa7.v1.order.infra.repo;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.msa7.v1.order.domain.aggregate.Order;
import com.msa7.v1.order.infra.entity.OrderJpaEntity;

// spring Data
public interface OrderJpaRepo extends JpaRepository<OrderJpaEntity, UUID> {
}
