package com.sparta.userservice.presentation.dto.response;

import com.sparta.userservice.domain.model.UserAddress;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class UserAddressResponse {

    private final UUID userAddressId;

    private final UUID userId;

    private final String addressName;

    private final String address;

    private final boolean isDefault;

    public static UserAddressResponse of(UserAddress userAddress) {
        return new UserAddressResponse(
                userAddress.getUserAddressId(),
                userAddress.getUserId(),
                userAddress.getAddressName(),
                userAddress.getAddress(),
                userAddress.isDefault()
        );
    }
}