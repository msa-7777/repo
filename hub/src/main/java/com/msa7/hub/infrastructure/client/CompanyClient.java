package com.msa7.hub.infrastructure.client;

import com.msa7.hub.global.response.RestApiResponse;
import com.msa7.hub.infrastructure.client.dto.CompanyResponse;
import com.msa7.hub.infrastructure.client.dto.PageResponse;
import com.msa7.hub.infrastructure.client.fallback.CompanyClientFallbackFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.UUID;

@FeignClient(name = "company-service", fallbackFactory = CompanyClientFallbackFactory.class)
public interface CompanyClient {
    @GetMapping("/api/v1/companies")
    RestApiResponse<PageResponse<CompanyResponse>> getCompanyList(@RequestParam UUID hubId);

}
