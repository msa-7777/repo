package com.sparta.userservice.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UserAddressCreateRequest {

    @NotBlank(message = "주소 별칭은 필수입니다.")
    @Size(max = 255, message = "주소 별칭은 255자 이하여야 합니다.")
    private String addressName;

    @NotBlank(message = "주소는 필수입니다.")
    @Size(max = 255, message = "주소는 255자 이하여야 합니다.")
    private String address;
}