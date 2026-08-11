package com.sparta.productservice.presentation.inventory;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/internal/inventories")
public class InventoryInternalController {

    @GetMapping("/{productId}")
    void verifyInventory(
            @PathVariable("productId") UUID productId,
            @RequestParam("quantity") Integer quantity) {

    }
}
