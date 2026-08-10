package com.msa7.v1.order.infra.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

// @FeignClient(name = "user-service")
public interface UserClient {
	// getMapping -> 유저에게 정보(최종주소)를 요청

}
