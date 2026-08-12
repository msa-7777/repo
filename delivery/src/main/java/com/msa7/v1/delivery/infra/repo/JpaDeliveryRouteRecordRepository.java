package com.msa7.v1.delivery.infra.repo;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.msa7.v1.delivery.domain.vo.RouteStatus;
import com.msa7.v1.delivery.infra.entity.DeliveryRouteRecordEntity;

public interface JpaDeliveryRouteRecordRepository extends JpaRepository<DeliveryRouteRecordEntity, UUID> {

	List<DeliveryRouteRecordEntity> findAllByStartHubIdAndStatusAndDeletedAtIsNull(UUID startHubId, RouteStatus status);

	@Query("SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END " +
		"FROM DeliveryRouteRecordEntity r " +
		"WHERE (r.startHubId = :hubId OR r.endHubId = :hubId) " +
		"AND r.status != 'ARRIVED'")
	boolean existsActiveRouteByHubId(@Param("hubId") UUID hubId);
}
