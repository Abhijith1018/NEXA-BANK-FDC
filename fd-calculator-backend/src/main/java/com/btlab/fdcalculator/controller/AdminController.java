package com.btlab.fdcalculator.controller;

import com.btlab.fdcalculator.model.dto.CategoryDTO;
import com.btlab.fdcalculator.service.ProductRuleSyncService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final ProductRuleSyncService productRuleSyncService;

    /**
     * Sync product rules from Product & Pricing API to Category repository
     * @param productCode The product code to sync (e.g., "FD001")
     */
    @PostMapping("/sync-product-rules/{productCode}")
    public ResponseEntity<Map<String, String>> syncProductRules(@PathVariable String productCode) {
        try {
            productRuleSyncService.syncProductRulesToCategories(productCode);
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Successfully synced product rules for " + productCode
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "status", "error",
                "message", "Failed to sync product rules: " + e.getMessage()
            ));
        }
    }

    /**
     * Get all synced categories from the database
     */
    @GetMapping("/categories")
    public ResponseEntity<List<CategoryDTO>> getAllCategories() {
        return ResponseEntity.ok(productRuleSyncService.getAllCategories());
    }
}
