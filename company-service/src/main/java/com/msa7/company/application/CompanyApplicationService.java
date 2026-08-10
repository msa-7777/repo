package com.msa7.company.application;

import com.msa7.company.domain.model.Company;
import com.msa7.company.infrastructure.client.hub.HubClient;
import com.msa7.company.infrastructure.client.user.UserClient;
import com.msa7.company.infrastructure.client.user.UserResponse;
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
    private final UserClient userClient;

    // 1. 업체 등록 (Create)
    @Transactional
    public CompanyResponse createCompany(CreateCompanyRequest request, UUID userId, String role) {

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
    public CompanyResponse updateCompany(UUID companyId, UpdateCompanyRequest request, UUID userId, String role) {
        Company company = findActiveCompany(companyId);

        validateCompanyAccess(company, userId, role);

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
    public void deleteCompany(UUID companyId, UUID userId, String role) {
        Company company = findActiveCompany(companyId);
        validateCompanyAccess(company, userId, role);
        company.delete(userId);
    }

    // 내부 공통 메서드: 삭제되지 않은 업체 검증 및 조회
    private Company findActiveCompany(UUID companyId) {
        return companyRepository.findByCompanyIdAndDeletedAtIsNull(companyId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COMPANY_NOT_FOUND));
    }

    /**
     * 허브 관리자 및 업체 담당자의 본인 업체 관리 권한 검증
     */
    private void validateCompanyAccess(Company targetCompany, UUID userId, String role) {
        // MASTER 권한은 전체 관리 가능
        if ("MASTER".equals(role)) {
            return;
        }

        // user-service에서 호출한 유저 정보 (hubId, supplierId 포함)
        UserResponse userInfo = userClient.getUserById(userId);
        if (userInfo == null) {
            throw new BusinessException(ErrorCode.USER_ACCESS_DENIED);
        }

        // COMPANY 담당자인 경우 본인 업체 검증
        if ("COMPANY".equals(role)) {
            if (userInfo.getSupplierId() == null) {
                throw new BusinessException(ErrorCode.COMPANY_NOT_FOUND);
            }

            if (targetCompany.getCompanyId() == null || !userInfo.getSupplierId().equals(targetCompany.getCompanyId())) {
                throw new BusinessException(ErrorCode.COMPANY_ACCESS_DENIED);
            }
            return;
        }

        // HUB_MANAGER: 본인 허브에 속한 업체인지 검증
        if ("HUB_MANAGER".equals(role)) {
            if (userInfo.getHubId() == null) {
                throw new BusinessException(ErrorCode.HUB_NOT_FOUND);
            }

            // 업체의 hubId와 허브 관리자의 hubId 비교
            if (targetCompany.getHubId() == null || !userInfo.getHubId().equals(targetCompany.getHubId())) {
                throw new BusinessException(ErrorCode.HUB_ACCESS_DENIED);
            }
            return;
        }

        throw new BusinessException(ErrorCode.FORBIDDEN);
    }
}