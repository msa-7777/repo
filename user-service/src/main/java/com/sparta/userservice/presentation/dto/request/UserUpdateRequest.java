package com.sparta.userservice.presentation.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class UserUpdateRequest {

    @Size(max = 32, message = "이름은 32자 이하이어야 합니다.")
    private String name;

    @Size(max = 32, message = "연락처는 32자 이하이어야 합니다.")
    @Pattern(
            regexp = "^(0\\d{1,2}-\\d{3,4}-\\d{4}|1\\d{3}-\\d{4})$",
            message = "올바른 연락처 형식이 아닙니다."
    )
    private String phone;

    @Size(max = 32, message = "이메일은 32자 이하이어야 합니다.")
    @Pattern(
            regexp = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$",
            message = "올바른 이메일 형식이 아닙니다."
    )
    private String email;

    @Size(min = 8, max = 15, message = "비밀번호는 8자 이상 15자 이하이어야 합니다.")
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*])[A-Za-z\\d!@#$%^&*]+$",
            message = "비밀번호는 영문 대/소문자, 숫자, 특수문자를 모두 포함해야 합니다."
    )
    private String password;

    @Size(max = 64, message = "Slack ID는 64자 이하이어야 합니다.")
    private String slackId;

    @JsonIgnore // Jackson에게 getter 처럼 보이지만, JSON Property로 취급하지 않도록 설정
    public List<String> getUpdateFields() {
        List<String> updateFields = new ArrayList<>();
        if (this.name != null) updateFields.add("name");
        if (this.phone != null) updateFields.add("phone");
        if (this.email != null) updateFields.add("email");
        if (this.password != null) updateFields.add("password");
        if (this.slackId != null) updateFields.add("slackId");
        return updateFields;
    }
}