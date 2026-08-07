package com.msa7.ai.global.config;

import org.springframework.data.domain.AuditorAware;
import org.springframework.stereotype.Component;
import java.util.Optional;
import java.util.UUID;

@Component
public class SpringAuditorAware implements AuditorAware<UUID> {
    @Override
    public Optional<UUID> getCurrentAuditor() {
        // TODO: 시큐리티 세터나 HTTP 헤더(예: X-User-Id 등)에서 현재 사용자 UUID를 추출해서 리턴
        // 임테스트용 고정 UUID 반환 (필요에 맞게 수정)
        return Optional.of(UUID.fromString("11111111-1111-1111-1111-111111111111"));
    }
}