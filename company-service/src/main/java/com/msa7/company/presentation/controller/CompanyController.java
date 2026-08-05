package com.msa7.company.presentation.controller;

import com.msa7.company.application.CompanyApplicationService;
import com.msa7.company.domain.model.CompanySearchCondition;
import com.msa7.company.global.common.RestApiResponse;
import com.msa7.company.presentation.dto.request.UpdateCompanyRequest;
import com.msa7.company.presentation.dto.response.CompanyResponse;
import com.msa7.company.presentation.dto.request.CreateCompanyRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/companies")
@RequiredArgsConstructor
public class CompanyController {

    private final CompanyApplicationService companyApplicationService;

    // 1. 업체 등록
    @PostMapping
    public RestApiResponse<CompanyResponse> createCompany(@Valid @RequestBody CreateCompanyRequest request) {
        CompanyResponse response = companyApplicationService.createCompany(request);
        return RestApiResponse.ok("업체가 성공적으로 등록되었습니다.", response);
    }

    // 2. 업체 단건 조회
    @GetMapping("/{companyId}")
    public RestApiResponse<CompanyResponse> getCompany(@PathVariable UUID companyId) {
        CompanyResponse response = companyApplicationService.getCompany(companyId);
        return RestApiResponse.ok("업체 정보 조회가 완료되었습니다.", response);
    }

    // 3. 업체 목록 검색 및 페이징
    @GetMapping
    public RestApiResponse<Page<CompanyResponse>> getCompanies(
            @ModelAttribute CompanySearchCondition condition,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<CompanyResponse> response = companyApplicationService.searchCompanies(condition, pageable);
        return RestApiResponse.ok("업체 목록 조회가 완료되었습니다.", response);
    }

    // 4. 업체 정보 수정
    @PatchMapping("/{companyId}")
    public RestApiResponse<CompanyResponse> updateCompany(
            @PathVariable UUID companyId,
            @Valid @RequestBody UpdateCompanyRequest request
    ) {
        CompanyResponse response = companyApplicationService.updateCompany(companyId, request);
        return RestApiResponse.ok("업체 정보가 정상적으로 수정되었습니다.", response);
    }

    // #TODO : userId 수정 요망
    // 5. 업체 삭제
    @DeleteMapping("/{companyId}")
    public RestApiResponse<Void> deleteCompany(
            @PathVariable UUID companyId,
            @RequestHeader(value = "X-User-Id", required = false) UUID userId // 추후 게이트웨이 연동 헤더
    ) {
        UUID deletedBy = (userId != null) ? userId : UUID.fromString("11111111-1111-1111-1111-111111111111");
        companyApplicationService.deleteCompany(companyId, deletedBy);
        return RestApiResponse.ok("업체가 성공적으로 삭제되었습니다.", null);
    }
}