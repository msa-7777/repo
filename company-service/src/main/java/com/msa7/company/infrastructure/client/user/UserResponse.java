package com.msa7.company.infrastructure.client.user;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@NoArgsConstructor
public class UserResponse {
    private UUID userId;
    private UUID hubId;
    private UUID supplierId;
}