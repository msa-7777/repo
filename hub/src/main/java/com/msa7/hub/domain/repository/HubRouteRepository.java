package com.msa7.hub.domain.repository;

import com.msa7.hub.domain.model.HubRoute;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface HubRouteRepository extends JpaRepository<HubRoute, UUID> {

    boolean existsByFromHubIdAndToHubId(UUID fromHubId, UUID toHubId);
}
