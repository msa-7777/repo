package com.msa7.hub.infrastructure.persistence;

import com.msa7.hub.domain.model.Hub;
import com.msa7.hub.domain.model.HubState;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface HubRepository extends JpaRepository<Hub, UUID>, HubRepositoryCustom {

    Optional<Hub> findByIdAndDeletedAtIsNull(UUID hubId);

    boolean existsByCentralHubIdAndDeletedAtIsNull(UUID centralHubId);

    boolean existsByIdAndHubStateAndDeletedAtIsNull(UUID hubId, HubState hubState);
}
