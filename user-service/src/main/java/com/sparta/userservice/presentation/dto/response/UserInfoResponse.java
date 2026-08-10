package com.sparta.userservice.presentation.dto.response;

import com.sparta.userservice.domain.model.Role;
import com.sparta.userservice.domain.model.SignupStatus;
import com.sparta.userservice.domain.model.User;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor(access = AccessLevel.PUBLIC)
public class UserInfoResponse {
    private final UUID userId;

    private final String loginId;

    private final String password; // 보안때문에 넣지 않는 방향으로...

    private final String name;

    private final String email;

    private final String phone;

    private final Role role;

    private final SignupStatus signupStatus;

    private final String slackId;

    private final UUID hubId;

    private final UUID supplierId;

    // static factory method (객체를 만들어서 반환하는 함수)
    public static UserInfoResponse of(User user) {
        return new UserInfoResponse(
                user.getUserId(),
                user.getLoginId(),
                user.getPassword(),
                user.getName(),
                user.getEmail(),
                user.getPhone(),
                user.getRole(),
                user.getSignupStatus(),
                user.getSlackId(),
                user.getHubId(),
                user.getSupplierId()
        );
    }
}
