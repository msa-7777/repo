package com.msa7.company.domain.repository;

import com.msa7.company.domain.model.Company;
import com.msa7.company.presentation.dto.request.CompanySearchCondition;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CompanyRepositoryCustom {
    Page<Company> searchCompanies(CompanySearchCondition condition, Pageable pageable);
}