package com.msa7.ai.infrastructure.client.product;

import com.msa7.ai.global.response.RestApiResponse;
import com.msa7.ai.infrastructure.client.product.ProductResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(name = "product-service")
public interface ProductClient {

    @GetMapping("/api/v1/products/{productId}")
    //ProductResponse getProduct(@PathVariable("productId") UUID productId);
    ResponseEntity<RestApiResponse<ProductResponse>> getProduct(@PathVariable("productId") UUID productId);
}