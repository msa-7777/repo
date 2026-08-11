package com.msa7.hub.infrastructure.persistence;

import com.msa7.hub.domain.model.HubRoute;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface HubRouteRepositoryCustom {

    Page<HubRoute> search(UUID fromHubId, UUID toHubId, Pageable pageable);
}
