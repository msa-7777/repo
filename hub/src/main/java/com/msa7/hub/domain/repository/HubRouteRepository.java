package com.msa7.hub.domain.repository;

import com.msa7.hub.domain.model.HubRoute;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface HubRouteRepository extends JpaRepository<HubRoute, UUID> {

    boolean existsByFromHubIdAndToHubIdAndDeletedAtIsNull(UUID fromHubId, UUID toHubId);

    Optional<HubRoute> findByIdAndDeletedAtIsNull(UUID hubRouteId);

    Optional<HubRoute> findByFromHubIdAndToHubIdAndDeletedAtIsNull(UUID fromHubId, UUID toHubId);
}
