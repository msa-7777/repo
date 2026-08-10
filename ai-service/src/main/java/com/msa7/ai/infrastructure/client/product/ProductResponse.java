package com.msa7.ai.infrastructure.client.product;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ProductResponse(
        UUID productId,
        String name,
        UUID companyId
) {}