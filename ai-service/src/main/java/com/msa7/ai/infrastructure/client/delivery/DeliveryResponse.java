package com.msa7.ai.infrastructure.client.delivery;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public record DeliveryResponse(
//        UUID id,
//        UUID orderId,
//        String status,
////        UUID departureHubId,  // 발송지
//        UUID receiverId,
//        String destinationAddress,
//        List<RouteRecordResponse> routeRecords
    UUID deliveryId,
    UUID startHubId,
    UUID endHubId,
    String destinationAddress,
    UUID companyManagerId,
    List<DeliveryRouteInfoDetails> routeRecords

) {
    public record DeliveryRouteInfoDetails(
            Integer sequence,
            UUID hubManagerId,
            String status
    ){}
//    public record RouteRecordResponse(
//            Integer sequence,
//            UUID startHubId,
//            UUID endHubId,
//            UUID deliveryManagerId,
//            Long estimatedDistance, // 예상거리
//            Long estimatedTime // 예상 소요 시간
//    ) {}
}