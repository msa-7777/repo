package com.msa7.company.infrastructure.client.hub;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;
// #TODO : hub 확인 후 수정 요망
@Getter
@NoArgsConstructor
public class HubResponse {
    private UUID id;
    private String name;
    private String address;
}