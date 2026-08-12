package com.sparta.userservice.domain.model;

import com.sparta.userservice.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "p_user_address")
public class UserAddress extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "user_address_id")
    private UUID userAddressId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "address_name", nullable = false)
    private String addressName;

    @Column(name = "address", nullable = false)
    private String address;

    @Column(name = "is_default", nullable = false)
    private boolean isDefault;

    public UserAddress(UUID userId, String addressName, String address) {
        this.userId = userId;
        this.addressName = addressName;
        this.address = address;
        this.isDefault = false;
    }

    public void setDefault() { this.isDefault = true; }

    public void unsetDefault() { this.isDefault = false; }
}