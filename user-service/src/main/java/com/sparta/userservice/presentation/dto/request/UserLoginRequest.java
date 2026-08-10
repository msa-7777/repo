package com.sparta.userservice.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class UserLoginRequest {

    @NotBlank(message = "로그인 아이디는 필수 항목입니다.")
    @Size(min = 4, max = 10, message = "로그인 ID는 4자 이상, 10자 이하이어야 합니다.")
    @Pattern(regexp = "^[a-z0-9]+$", message = "로그인 ID는 소문자와 숫자만 사용할 수 있습니다.")
    private String loginId;

    @NotBlank(message = "비밀번호는 필수 항목입니다.")
    @Size(min = 8, max = 15, message = "비밀번호는 8자 이상, 15자 이하이어야 합니다.")
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]+$",
            message = "비밀번호는 영문 대/소문자, 숫자, 특수문자를 모두 포함해야 합니다."
    )
    private String password;
}