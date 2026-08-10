package com.msa7.v1.delivery.presentation;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.msa7.v1.delivery.app.DeliveryManagerService;
import com.msa7.v1.delivery.app.DeliveryService;
import com.msa7.v1.delivery.domain.vo.RouteStatus;
import com.msa7.v1.delivery.presentation.dto.CreateDeliveryRequest;
import com.msa7.v1.delivery.presentation.dto.CreateManagerRequest;
import com.msa7.v1.delivery.presentation.dto.RestApiResponse;
import com.msa7.v1.delivery.presentation.dto.UpdateDeliveryStatusRequest;
import com.msa7.v1.delivery.presentation.dto.payload.DeliveryResponse;
import com.msa7.v1.delivery.presentation.dto.payload.DeliveryRouteResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/deliveries")
@RequiredArgsConstructor
public class DeliveryController {

	private final DeliveryService deliveryService;
	private final DeliveryManagerService deliveryManagerService;

	// 배송 담당자 등록
	@PostMapping("/managers")
	public ResponseEntity<RestApiResponse<UUID>> createManager(
		@RequestHeader("X-User-Id") UUID userId,
		@RequestBody CreateManagerRequest request) {

		// 요청된 사용자 ID를 기반으로 생성
		UUID id = deliveryManagerService.createDeliveryManager(request.userId(), request.slackId(), request.hubId(), request.type());
		return ResponseEntity.ok(RestApiResponse.ok( "배송 담당자가 생성되었습니다.", id));
	}

	// 배송 담당자 삭제 (논리적 삭제)
	@DeleteMapping("/managers/{id}")
	public ResponseEntity<RestApiResponse<Void>> deleteManager(
		@RequestHeader("X-User-Id") UUID deletedBy,
		@PathVariable UUID id) {

		deliveryManagerService.deleteDeliveryManager(id, String.valueOf(deletedBy));
		return ResponseEntity.ok(RestApiResponse.ok( "배송 담당자가 삭제되었습니다.", null));
	}

	@PostMapping
	public ResponseEntity<RestApiResponse<UUID>> createDelivery(
		@RequestHeader("X-User-Id") UUID userId,
		@RequestBody CreateDeliveryRequest request) {

		UUID id = deliveryService.createDelivery(
			request.orderId(), request.startHubId(), request.endHubId(), request.destinationAddress(),
			request.receiverName(), request.receiverSlackId()
		);
		return ResponseEntity.ok(RestApiResponse.ok("배송이 생성되었습니다.", id));
	}

	// 배송 상태 변경
	@PatchMapping("/{deliveryId}/status")
	public ResponseEntity<RestApiResponse<Void>> updateStatus(
		@PathVariable UUID deliveryId,
		@RequestBody UpdateDeliveryStatusRequest request) {

		deliveryService.updateDeliveryStatus(deliveryId, request.status());

		return ResponseEntity.ok(RestApiResponse.ok( "배송 상태가 업데이트되었습니다." , null));
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<RestApiResponse<Void>> deleteDelivery(
		@RequestHeader("X-User-Id") UUID deletedBy,
		@PathVariable UUID id) {

		deliveryService.deleteDelivery(id, deletedBy);
		return ResponseEntity.ok(RestApiResponse.ok( "배송이 삭제되었습니다.", null));
	}

	// 우태님 요청사항 api : 허브 ID와 경로 상태 기준
	@GetMapping("/routes")
	public ResponseEntity<RestApiResponse<List<DeliveryRouteResponse>>> getDeliveryRoutes(
		@RequestParam UUID hubId,
		@RequestParam RouteStatus status) {

		List<DeliveryRouteResponse> responses = deliveryService.getDeliveryRoutes(hubId, status);
		return ResponseEntity.ok(RestApiResponse.ok( "배송 경로 조회가 완료되었습니다.", responses));
	}

	// 필규님 요청사항 api: 배송 ID 기준 (상태 포함)
	@GetMapping("/{deliveryId}")
	public ResponseEntity<RestApiResponse<DeliveryResponse>> getDeliveryInfo(
		@PathVariable UUID deliveryId) {

		DeliveryResponse response = deliveryService.getDeliveryInfo(deliveryId);
		return ResponseEntity.ok(RestApiResponse.ok( "배송 단건 조회가 완료되었습니다.", response));
	}


}