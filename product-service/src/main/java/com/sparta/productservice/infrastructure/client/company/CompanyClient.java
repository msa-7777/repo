package com.sparta.productservice.infrastructure.client.company;

import com.sparta.productservice.global.response.RestApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(name = "company-service")
public interface CompanyClient {

    /**
     * 기존 업체 단건 조회 API를 재사용한다.
     *
     * TODO: 실제 company-service의 Eureka 등록 '이름'과
     *       업체 단건 조회 'URL'이 아래 값과 일치하는지 확인한다.
     */
    @GetMapping("/api/v1/companies/{companyId}")
    RestApiResponse<CompanyResponse> getCompany(
            @PathVariable("companyId") UUID companyId
    );
}