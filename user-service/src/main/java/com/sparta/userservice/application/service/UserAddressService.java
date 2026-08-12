package com.sparta.userservice.application.service;

import com.sparta.userservice.application.port.UserFinder;
import com.sparta.userservice.domain.model.UserAddress;
import com.sparta.userservice.domain.repository.UserAddressRepository;
import com.sparta.userservice.presentation.dto.request.UserAddressCreateRequest;
import com.sparta.userservice.presentation.dto.response.UserAddressResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserAddressService {

    private final UserFinder userFinder;
    private final UserAddressRepository userAddressRepository;

    @Transactional
    public UserAddressResponse createAddress(UUID userId, UserAddressCreateRequest request) {
        // 1. 사용자 존재 여부 확인
        userFinder.getUserById(userId);

        // 2. 해당 사용자의 기존 주소 존재 여부 확인
        boolean hasAddress = userAddressRepository.existsByUserIdAndIsDeletedFalse(userId);

        // 3. 주소 생성
        UserAddress userAddress = new UserAddress(
                userId,
                request.getAddressName(),
                request.getAddress()
        );

        // 4. 첫 번째 주소라면 기본 주소로 설정
        if (!hasAddress) {
            userAddress.setDefault();
        }

        // 5. 저장
        UserAddress savedAddress = userAddressRepository.save(userAddress);

        return UserAddressResponse.of(savedAddress);
    }
}