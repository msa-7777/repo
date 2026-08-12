package com.sparta.userservice.domain.repository;

import com.sparta.userservice.domain.model.UserAddress;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserAddressRepository extends JpaRepository<UserAddress, UUID> {

    Optional<UserAddress> findByUserIdAndIsDefaultTrueAndIsDeletedFalse(UUID userId);

    boolean existsByUserIdAndIsDeletedFalse(UUID userId);
}