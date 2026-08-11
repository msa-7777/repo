package com.sparta.productservice.infrastructure.client.user;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.UUID;

/**
 * user-service 사용자 단건 조회 응답 중 상품 접근 권한 검증에 필요한 정보만 받는다.
 *
 * SUPPLIER_AGENT
 * - supplierId는 생산 업체의 companyId를 의미한다.
 * - 상품의 companyId와 비교하여 본인 업체 상품인지 검증한다.
 *
 * HUB_MANAGER
 * - hubId는 사용자가 담당하는 허브를 의미한다.
 * - 업체 또는 재고의 hubId와 비교하여 담당 허브인지 검증한다.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record UserResponse(
        UUID userId,
        UUID hubId,
        UUID supplierId
) {
}