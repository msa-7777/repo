package com.sparta.userservice.presentation.dto.response;

import com.sparta.userservice.domain.model.Role;
import com.sparta.userservice.domain.model.SignupStatus;
import com.sparta.userservice.domain.model.User;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class UserSignupResponse {

    // userId, loginId, name, email, role, signupStatus
    private final UUID userId;

    private final String loginId;

    private final String name;

    private final String email;

    private final Role role;

    private final SignupStatus signupStatus;

    public static UserSignupResponse of(User user) {
        return new UserSignupResponse(
                user.getUserId(),
                user.getLoginId(),
                user.getName(),
                user.getEmail(),
                user.getRole(),
                user.getSignupStatus()
        );
    }
}
