package com.sparta.productservice.presentation.inventory;

import com.sparta.productservice.application.inventory.InventoryService;
import com.sparta.productservice.domain.inventory.InventorySearchCondition;
import com.sparta.productservice.global.response.RestApiResponse;
import com.sparta.productservice.presentation.inventory.request.InventoryQuantityChangeRequest;
import com.sparta.productservice.presentation.inventory.response.InventoryResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;
@Tag(name = "Inventory", description = "재고 관리 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/inventories")
public class InventoryController {

    private final InventoryService inventoryService;

    // 상품별 재고 단건 조회.
    /*
     * 로그인 사용자가 조회할 수 있으며,
     * URL의 식별자는 inventoryId가 아니라 productId다.
     */
    @Operation(
            summary = "재고 단건 조회",
            description = "상품 ID로 재고 정보를 조회합니다."
    )
    @GetMapping("/{productId}")
    public ResponseEntity<RestApiResponse<InventoryResponse>> getInventory(
            @PathVariable UUID productId
    ) {
        InventoryResponse response = inventoryService.getInventory(productId);

        return ResponseEntity.ok(
                RestApiResponse.success(
                        HttpStatus.OK,
                        "재고가 조회되었습니다.",
                        response
                )
        );
    }

     // 재고 목록 및 검색.
     /*
     * 검색 조건:
     * - hubId
     * - minQuantity
     * - maxQuantity
     */
     @Operation(
             summary = "재고 목록 조회",
             description = "검색 조건에 따라 재고 목록을 조회합니다."
     )
    @GetMapping
    public ResponseEntity<
            RestApiResponse<Page<InventoryResponse>>> getInventories(
            @ParameterObject
            @ModelAttribute
            InventorySearchCondition condition,

            @ParameterObject
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable
    ) {
        Page<InventoryResponse> response = inventoryService.getInventories(
                        condition,
                        pageable
                );

        return ResponseEntity.ok(
                RestApiResponse.success(
                        HttpStatus.OK,
                        "재고 목록이 조회되었습니다.",
                        response
                )
        );
    }

    // 재고 수량 변경.
    /* INBOUND:
     *   허브 관리자와 마스터 관리자가 수동으로 처리
     *
     * ORDER_DECREASE, ORDER_CANCEL_RESTORE:
     *   order-service에서 호출
     */
    @Operation(
            summary = "재고 수량 변경",
            description = "상품의 재고 수량을 변경합니다."
    )
    @PatchMapping("/{productId}/quantity")
    public ResponseEntity<RestApiResponse<InventoryResponse>> changeQuantity(
            @PathVariable UUID productId,
            @Valid
            @RequestBody
            InventoryQuantityChangeRequest request
    ) {
        InventoryResponse response = inventoryService.changeQuantity(
                        productId,
                        request
                );

        return ResponseEntity.ok(
                RestApiResponse.success(
                        HttpStatus.OK,
                        "재고 수량이 변경되었습니다.",
                        response
                )
        );
    }
}