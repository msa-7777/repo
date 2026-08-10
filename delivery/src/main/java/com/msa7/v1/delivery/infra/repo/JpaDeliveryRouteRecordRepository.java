package com.msa7.v1.delivery.infra.repo;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.msa7.v1.delivery.domain.aggregateDelivery.DeliveryRouteRecord;
import com.msa7.v1.delivery.domain.vo.RouteStatus;
import com.msa7.v1.delivery.infra.entity.DeliveryRouteRecordEntity;

public interface JpaDeliveryRouteRecordRepository extends JpaRepository<DeliveryRouteRecord, UUID> {

	List<DeliveryRouteRecordEntity> findAllByStartHubIdAndStatusAndIsDeletedFalse(UUID startHubId, RouteStatus status);
}
