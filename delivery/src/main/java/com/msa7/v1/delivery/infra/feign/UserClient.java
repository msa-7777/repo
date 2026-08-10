package com.msa7.v1.delivery.infra.feign;

import java.util.UUID;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "user-service")
public interface UserClient {
	// 다음 순번의 배송 담당자 조회
	@GetMapping("/api/v1/users/delivery-managers/next")
	UUID getNextDeliveryManagerId(@RequestParam("hubId") UUID hubId);
}
