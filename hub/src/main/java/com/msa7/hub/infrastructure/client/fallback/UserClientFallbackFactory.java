package com.msa7.hub.infrastructure.client.fallback;

import com.msa7.hub.global.exception.BusinessException;
import com.msa7.hub.global.exception.ErrorCode;
import com.msa7.hub.infrastructure.client.UserClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class UserClientFallbackFactory implements FallbackFactory<UserClient> {
    @Override
    public UserClient create(Throwable cause) {
        return hubId -> {
            log.warn("user-service 호출 실패, fallback 실행 - hubId={}", hubId, cause);
            throw new BusinessException(ErrorCode.EXTERNAL_SERVICE_UNAVAILABLE);
        };
    }
}
