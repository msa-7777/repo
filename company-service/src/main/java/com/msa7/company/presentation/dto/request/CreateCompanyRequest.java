package com.msa7.company.presentation.dto.request;

import com.msa7.company.domain.model.CompanyType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@NoArgsConstructor
public class CreateCompanyRequest {

    @NotBlank(message = "업체명은 필수 입력 항목입니다.")
    private String name;

    @NotNull(message = "업체 타입은 필수 항목입니다.")
    private CompanyType type;

    @NotNull(message = "관리 허브 ID는 필수 항목입니다.")
    private UUID hubId;

    @NotBlank(message = "업체 주소는 필수 입력 항목입니다.")
    private String address;
}