package com.msa7.ai.infrastructure.client.order;

import java.util.UUID;

public record OrderWithDeliveryDto(UUID orderId,
                                   String orderStatus,
                                   String deliveryStatus,
                                   UUID deliveryId)
{ }