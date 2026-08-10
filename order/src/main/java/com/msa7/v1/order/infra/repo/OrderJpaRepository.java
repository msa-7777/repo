package com.msa7.v1.order.infra.repo;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.msa7.v1.order.infra.entity.OrderEntity;

// spring Data
public interface OrderJpaRepository extends JpaRepository<OrderEntity, UUID> {
}
