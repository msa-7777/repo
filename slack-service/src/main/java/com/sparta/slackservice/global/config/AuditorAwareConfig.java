package com.sparta.slackservice.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;

import java.util.Optional;
import java.util.UUID;

@Configuration
public class AuditorAwareConfig {

    @Bean
    public AuditorAware<UUID> auditorProvider() {
        return () -> {

            // TODO: Gateway에서 전달한 JWT의 사용자 UUID를 SecurityContext에서 조회하여 반환

            return Optional.of(
                    UUID.fromString("00000000-0000-0000-0000-000000000001")
            );
        };
    }
}
