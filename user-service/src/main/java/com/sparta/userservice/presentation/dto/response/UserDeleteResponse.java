package com.sparta.userservice.presentation.dto.response;

import com.sparta.userservice.domain.model.User;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class UserDeleteResponse {

    private final UUID userId;

    public static UserDeleteResponse of(User user) {
        return new UserDeleteResponse(
                user.getUserId()
        );
    }
}