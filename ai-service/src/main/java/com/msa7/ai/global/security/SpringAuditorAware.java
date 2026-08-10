package com.msa7.ai.global.security;

import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import java.util.Optional;
import java.util.UUID;

@Component
public class SpringAuditorAware implements AuditorAware<UUID> {
    @Override
    public Optional<UUID> getCurrentAuditor() {
        // 1. 현재 SecurityContext에서 Authentication 객체를 가져옵니다.
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // 2. 인증 정보가 없거나, 인증되지 않은 사용자(익명 사용자 등)인 경우 빈 Optional 반환
        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            return Optional.empty();
        }

        // 3. HeaderAuthenticationFilter에서 principal에 저장한 userId(String) 추출 후 UUID로 변환
        try {
            String userIdStr = (String) authentication.getPrincipal();
            return Optional.of(UUID.fromString(userIdStr));
        } catch (Exception e) {
            // UUID 형식이 아닌 예외 상황 등에 대비해 빈 값 반환
            return Optional.empty();
        }
    }
}