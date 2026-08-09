package com.msa7.company.application;

import com.msa7.company.domain.model.Company;
import com.msa7.company.infrastructure.client.HubClient;
import com.msa7.company.presentation.dto.request.CompanySearchCondition;
import com.msa7.company.domain.repository.CompanyRepository;
import com.msa7.company.global.exception.BusinessException;
import com.msa7.company.global.exception.ErrorCode;
import com.msa7.company.presentation.dto.request.UpdateCompanyRequest;
import com.msa7.company.presentation.dto.request.CreateCompanyRequest;
import com.msa7.company.presentation.dto.response.CompanyResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CompanyApplicationService {

    private final CompanyRepository companyRepository;
    private final HubClient hubClient;

    // 1. 업체 등록 (Create)
    @Transactional
    public CompanyResponse createCompany(CreateCompanyRequest request) {
        // #TODO : hub 확인후 수정 요망
        if (!hubClient.existsHub(request.hubId())) {
            throw new BusinessException(ErrorCode.HUB_NOT_FOUND);
        }

        if (companyRepository.existsByNameAndDeletedAtIsNull(request.name())) {
            throw new BusinessException(ErrorCode.DUPLICATE_COMPANY_NAME);
        }

        Company company = Company.create(
                request.name(),
                request.type(),
                request.hubId(),
                request.address()
        );

        Company savedCompany = companyRepository.save(company);
        return CompanyResponse.from(savedCompany);
    }


    // 2. 업체 단건 조회 (Read)
    public CompanyResponse getCompany(UUID companyId) {
        Company company = findActiveCompany(companyId);
        return CompanyResponse.from(company);
    }

    // 3. 업체 목록 동적 검색 및 페이징 (Search - QueryDSL)
    public Page<CompanyResponse> searchCompanies(CompanySearchCondition condition, Pageable pageable) {
        return companyRepository.searchCompanies(condition, pageable)
                .map(CompanyResponse::from);
    }

    // 4. 업체 수정 (Update)
    @Transactional
    public CompanyResponse updateCompany(UUID companyId, UpdateCompanyRequest request) {
        Company company = findActiveCompany(companyId);

        company.update(
                request.name(),
                request.type(),
                request.hubId(),
                request.address()
        );

        return CompanyResponse.from(company);
    }

    // 5. 업체 삭제 (Delete - Soft Delete)
    @Transactional
    public void deleteCompany(UUID companyId, UUID deletedBy) {
        Company company = findActiveCompany(companyId);
        company.delete(deletedBy);
    }

    // 내부 공통 메서드: 삭제되지 않은 업체 검증 및 조회
    private Company findActiveCompany(UUID companyId) {
        return companyRepository.findByCompanyIdAndDeletedAtIsNull(companyId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COMPANY_NOT_FOUND));
    }
}