package com.msa7.v1.order.infra.feign;

import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name = "delivery-service")
public interface DeliveryClient {


}
