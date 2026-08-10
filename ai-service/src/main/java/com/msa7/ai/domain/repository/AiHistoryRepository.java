package com.msa7.ai.domain.repository;

import com.msa7.ai.domain.model.AiHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface AiHistoryRepository extends JpaRepository<AiHistory, UUID>, AiHistoryRepositoryCustom {
    Optional<AiHistory> findByHistoryIdAndDeletedAtIsNull(UUID historyId);
    Optional<AiHistory> findByOrderIdAndDeletedAtIsNull(UUID orderId);
}