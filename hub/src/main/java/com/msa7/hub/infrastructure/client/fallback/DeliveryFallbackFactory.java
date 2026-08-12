package com.msa7.hub.infrastructure.client.fallback;

import com.msa7.hub.global.exception.BusinessException;
import com.msa7.hub.global.exception.ErrorCode;
import com.msa7.hub.infrastructure.client.DeliveryClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class DeliveryFallbackFactory implements FallbackFactory<DeliveryClient> {
    @Override
    public DeliveryClient create(Throwable cause) {
        return hubId -> {
            log.warn("delivery-service 호출 실패, fallback 실행 - hubId={}", hubId, cause);
            throw new BusinessException(ErrorCode.EXTERNAL_SERVICE_UNAVAILABLE);
        };
    }
}
