package com.msa7.company.domain.repository;

import com.msa7.company.domain.model.Company;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CompanyRepository extends JpaRepository<Company, UUID>, CompanyRepositoryCustom {
    // 삭제되지 않은 단건 업체 조회
    Optional<Company> findByCompanyIdAndDeletedAtIsNull(UUID companyId);

    // 이름 중복 확인
    boolean existsByNameAndDeletedAtIsNull(String name);
}