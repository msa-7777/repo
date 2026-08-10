package com.sparta.userservice.presentation.dto.request;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sparta.userservice.domain.model.Role;
import com.sparta.userservice.domain.model.SignupStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class AdminUserUpdateRequest {

    @Size(min = 4, max = 10, message = "로그인 ID는 4자 이상 10자 이하이어야 합니다.")
    @Pattern(
            regexp = "^[a-z0-9]+$",
            message = "로그인 ID는 영문 소문자와 숫자만 사용할 수 있습니다."
    )
    private String loginId;

    @Size(min = 8, max = 15, message = "비밀번호는 8자 이상 15자 이하이어야 합니다.")
    @Pattern(
            regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[^A-Za-z\\d]).+$",
            message = "비밀번호는 영문자, 숫자, 특수문자를 포함해야 합니다."
    )
    private String password;

    @Size(max = 16, message = "이름은 16자 이하이어야 합니다.")
    private String name;

    @Size(max = 32, message = "이메일은 32자 이하이어야 합니다.")
    @Email(message = "올바른 이메일 형식이 아닙니다.")
    private String email;

    @Size(max = 32, message = "연락처는 32자 이하이어야 합니다.")
    @Pattern(
            regexp = "^(0\\d{1,2}-\\d{3,4}-\\d{4}|1\\d{3}-\\d{4})$",
            message = "올바른 연락처 형식이 아닙니다."
    )
    private String phone;

    private Role role;

    private SignupStatus signupStatus;

    @Size(max = 64, message = "Slack ID는 64자 이하이어야 합니다.")
    private String slackId;

    private UUID hubId;

    private UUID supplierId;

    @JsonIgnore
    public List<String> getUpdateFields() {
        List<String> updateFields = new ArrayList<>();

        if (this.loginId != null) updateFields.add("loginId");
        if (this.password != null) updateFields.add("password");
        if (this.name != null) updateFields.add("name");
        if (this.email != null) updateFields.add("email");
        if (this.phone != null) updateFields.add("phone");
        if (this.role != null) updateFields.add("role");
        if (this.signupStatus != null) updateFields.add("signupStatus");
        if (this.slackId != null) updateFields.add("slackId");
        if (this.hubId != null) updateFields.add("hubId");
        if (this.supplierId != null) updateFields.add("supplierId");

        return updateFields;
    }
}