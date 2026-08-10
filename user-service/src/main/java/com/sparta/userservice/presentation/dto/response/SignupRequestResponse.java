package com.sparta.userservice.presentation.dto.response;

import com.sparta.userservice.domain.model.Role;
import com.sparta.userservice.domain.model.SignupStatus;
import com.sparta.userservice.domain.model.User;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@AllArgsConstructor(access = AccessLevel.PUBLIC)
public class SignupRequestResponse {

    private UUID userId;

    private String loginId;

    private String name;

    private String email;

    private String phone;

    private Role role;

    private String slackId;

    private UUID hubId;

    private UUID supplierId;

    private SignupStatus signupStatus;

    private LocalDateTime createdAt;

    public static SignupRequestResponse of(User user) {
        return new SignupRequestResponse(
                user.getUserId(),
                user.getLoginId(),
                user.getName(),
                user.getEmail(),
                user.getPhone(),
                user.getRole(),
                user.getSlackId(),
                user.getHubId(),
                user.getSupplierId(),
                user.getSignupStatus(),
                user.getCreatedAt()
        );
    }
}