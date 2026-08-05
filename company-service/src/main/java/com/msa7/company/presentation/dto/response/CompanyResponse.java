package com.msa7.company.presentation.dto.response;

import com.msa7.company.domain.model.Company;
import com.msa7.company.domain.model.CompanyType;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class CompanyResponse {

    private UUID id;
    private String name;
    private CompanyType type;
    private UUID hubId;
    private String address;

    public static CompanyResponse from(Company company) {
        return CompanyResponse.builder()
                .id(company.getCompanyId())
                .name(company.getName())
                .type(company.getType())
                .hubId(company.getHubId())
                .address(company.getAddress())
                .build();
    }
}