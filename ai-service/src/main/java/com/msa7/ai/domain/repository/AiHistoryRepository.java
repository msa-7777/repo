package com.msa7.ai.domain.repository;

import com.msa7.ai.domain.model.AiHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface AiHistoryRepository extends JpaRepository<AiHistory, UUID> {

    Optional<AiHistory> findByHistoryIdAndDeletedAtIsNull(UUID historyId);

    Page<AiHistory> findAllByDeletedAtIsNull(Pageable pageable);

    Page<AiHistory> findAllByOrderIdAndDeletedAtIsNull(UUID orderId, Pageable pageable);
}