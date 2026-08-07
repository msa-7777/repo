package com.sparta.productservice.infrastructure.client.company;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.UUID;

/**
 * 기존 업체 단건 조회 API 응답 중
 * product-service에서 필요한 필드만 선언한다.
 *
 * 업체 응답의 name, address, createdAt 등은 사용하지 않으므로
 * ignoreUnknown을 통해 무시한다.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record CompanyResponse(
        UUID companyId,
        CompanyType companyType,
        UUID hubId
) {
}
