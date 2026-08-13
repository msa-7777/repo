package com.sparta.userservice.application.port;

import com.sparta.userservice.domain.model.UserAddress;

import java.util.UUID;

public interface UserAddressFinder {

    UserAddress getDefaultAddress(UUID userId);
}