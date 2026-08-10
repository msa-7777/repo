package com.sparta.userservice.presentation.dto.request;

import com.sparta.userservice.domain.model.Role;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class UserSignupRequest {

    @NotBlank(message = "로그인 아이디는 필수 항목입니다.")
    @Size(min = 4, max = 10, message = "로그인 ID는 4자 이상, 10자 이하이어야 합니다.")
    @Pattern(
            regexp = "^[a-z0-9]+$",
            message = "로그인 ID는 소문자와 숫자만 사용할 수 있습니다."
    )
    private String loginId;


    @NotBlank(message = "비밀번호는 필수 항목입니다.")
    @Size(min = 8, max = 15, message = "비밀번호는 8자 이상, 15자 이하이어야 합니다.")
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*])[A-Za-z\\d!@#$%^&*]+$",
            message = "비밀번호는 영문 대/소문자, 숫자, 특수문자를 모두 포함해야 합니다."
    )
    private String password;


    @NotBlank(message = "이름은 필수 항목입니다.")
    @Size(max = 16, message = "이름은 16자 이하이어야 합니다.")
    private String name;


    @NotBlank(message = "연락처는 필수 항목입니다.")
    @Size(max = 32, message = "연락처는 32자 이하이어야 합니다.")
    @Pattern(
            regexp = "^(0\\d{1,2}-\\d{3,4}-\\d{4}|1\\d{3}-\\d{4})$",
            message = "올바른 연락처 형식이 아닙니다."
    )
    private String phone;


    @NotBlank(message = "이메일은 필수 항목입니다.")
    @Size(max = 32, message = "이메일은 32자 이하이어야 합니다.")
    @Pattern(
            regexp = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$",
            message = "올바른 이메일 형식이 아닙니다."
    )
    // @Email(message = "올바른 이메일 형식이 아닙니다.")
    private String email;


    @NotBlank(message = "Slack ID는 필수 항목입니다.")
    @Size(max = 64, message = "Slack ID는 64자 이하이어야 합니다.")
    private String slackId;


    @NotNull(message = "권한은 필수 항목입니다.")
    private Role role;


    // 회원가입 신청 시 null 허용, 승인 단계에서 Role에 따라 검증
    private UUID hubId;

    // 회원가입 신청 시 null 허용, 승인 단계에서 Role에 따라 검증
    private UUID supplierId;
}