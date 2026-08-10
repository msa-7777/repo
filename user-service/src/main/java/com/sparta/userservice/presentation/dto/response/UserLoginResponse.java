package com.sparta.userservice.presentation.dto.response;

import com.sparta.userservice.domain.model.Role;
import com.sparta.userservice.domain.model.User;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE) // of() 와 같은 정적 팩토리 메서드를 통해서만 객체 생성 가능
public class UserLoginResponse {

    private final String accessToken;

    private final UUID userId;

    private final String loginId;

    private final String name;

    private final Role role;

    public static UserLoginResponse of(User user, String accessToken) {
        return new UserLoginResponse(
                accessToken,
                user.getUserId(),
                user.getLoginId(),
                user.getName(),
                user.getRole()
        );
    }
}
