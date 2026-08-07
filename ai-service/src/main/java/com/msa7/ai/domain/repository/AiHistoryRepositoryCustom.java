package com.msa7.ai.domain.repository;

import com.msa7.ai.domain.model.AiHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.UUID;

public interface AiHistoryRepositoryCustom {
    Page<AiHistory> searchAiHistories(UUID orderId, Pageable pageable);
}