package com.sparta.userservice.application.service;

import com.sparta.userservice.application.port.UserAddressFinder;
import com.sparta.userservice.domain.exception.UserErrorCode;
import com.sparta.userservice.domain.model.UserAddress;
import com.sparta.userservice.domain.repository.UserAddressRepository;
import com.sparta.userservice.global.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserAddressFinderService implements UserAddressFinder {

    private final UserAddressRepository userAddressRepository;

    @Override
    public UserAddress getDefaultAddress(UUID userId) {

        return userAddressRepository
                .findByUserIdAndIsDefaultTrueAndIsDeletedFalse(userId)
                .orElseThrow(() -> new BusinessException(UserErrorCode.NOT_FOUND_DEFAULT_ADDRESS));
    }
}