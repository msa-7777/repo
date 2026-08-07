package com.msa7.v1.delivery.presentation;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.msa7.v1.delivery.app.DeliveryService;
import com.msa7.v1.delivery.presentation.dto.RestApiResponse;
import com.msa7.v1.delivery.presentation.dto.UpdateDeliveryStatusRequest;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/deliveries")
@RequiredArgsConstructor
public class DeliveryController {

	private final DeliveryService deliveryService;

	// 배송 상태 변경
	@PatchMapping("/{deliveryId}/status")
	public ResponseEntity<RestApiResponse<Void>> updateStatus(
		@PathVariable UUID deliveryId,
		@RequestBody UpdateDeliveryStatusRequest request) {

		deliveryService.updateDeliveryStatus(deliveryId, request.status(), request.currentHubId());

		return ResponseEntity.ok(RestApiResponse.ok( "배송 상태가 업데이트되었습니다.", null));
	}

	// 배송 목록 조회
	@GetMapping
	public ResponseEntity<RestApiResponse<Object>> getDeliveries(
		@RequestParam(defaultValue = "1") int page,
		@RequestParam(defaultValue = "10") int size) {
		// 생략 (QueryDSL 활용 등을 통한 조회)
		return ResponseEntity.ok(RestApiResponse.ok( "조회 완료", null));
	}
}