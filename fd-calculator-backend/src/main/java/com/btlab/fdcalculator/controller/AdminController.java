package com.btlab.fdcalculator.controller;

import com.btlab.fdcalculator.model.dto.CategoryDTO;
import com.btlab.fdcalculator.service.ProductRuleSyncService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Admin controller for system administration tasks
 * Includes product rule synchronization and category management
 */
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Tag(name = "Admin", description = "Administrative endpoints for product rule synchronization and category management")
public class AdminController {

    private final ProductRuleSyncService productRuleSyncService;

    @Operation(
        summary = "Sync product rules from Product & Pricing API",
        description = """
            Synchronize product rules (categories and their benefits) from the Product & Pricing API to the local database.
            
            **Purpose:**
            - Fetch latest category definitions from Product & Pricing API
            - Store them in local Category repository for faster access
            - Ensure consistency between pricing system and FD calculator
            
            **Process:**
            1. Calls Product & Pricing API's `/api/rules?product_code={productCode}` endpoint
            2. Retrieves all product rules (MIN001, MAX001, SR001, JR001, GOLD, etc.)
            3. Parses rule codes and extracts category information
            4. Updates or creates Category records in local database
            5. Returns success/failure status
            
            **Rule Naming Convention:**
            - **SR001, JR001, DY001**: Customer category rules
            - Last 3 digits are parsed to create category codes
            - Rule value becomes the benefit rate
            
            **When to Use:**
            - Initial system setup
            - After changes in Product & Pricing API
            - When new categories are added
            - Periodic refresh (e.g., daily via scheduler)
            
            **Example Rules:**
            - SR001 → SENIOR category with 0.75% benefit
            - GOLD001 → GOLD category with 0.25% benefit
            - EMP001 → EMP category with 1.00% benefit
            
            **⚠️ Admin Only:** This endpoint should be restricted to admin users in production
            """,
        parameters = {
            @Parameter(
                name = "productCode",
                description = "Product code to sync rules for (e.g., FD001, FD002)",
                required = true,
                example = "FD001"
            )
        }
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Sync completed successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(type = "object"),
                examples = @ExampleObject(
                    name = "Success Response",
                    value = """
                        {
                          "status": "success",
                          "message": "Successfully synced product rules for FD001"
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Sync failed (Product API unavailable or parsing error)",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(type = "object"),
                examples = @ExampleObject(
                    name = "Error Response",
                    value = """
                        {
                          "status": "error",
                          "message": "Failed to sync product rules: Connection refused"
                        }
                        """
                )
            )
        )
    })
    @PostMapping("/sync-product-rules/{productCode}")
    public ResponseEntity<Map<String, String>> syncProductRules(
        @PathVariable 
        @Parameter(description = "Product code", example = "FD001") 
        String productCode
    ) {
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

    @Operation(
        summary = "Get all categories from database",
        description = """
            Retrieve all customer categories currently stored in the local database.
            
            **Purpose:**
            - View all synced categories
            - Verify sync operation results
            - Check category configurations
            - Admin dashboard display
            
            **Returns:**
            Complete list of categories with:
            - Category ID (database primary key)
            - Category Code (e.g., SENIOR, GOLD)
            - Category Name (display name)
            - Benefit Rate (additional interest percentage)
            - Description (category details)
            
            **Use Cases:**
            - Admin verification after sync
            - Category configuration review
            - Debugging category-related issues
            - Audit trail
            
            **Difference from /api/fd/categories:**
            - This endpoint: Database records (admin view)
            - /api/fd/categories: Public API (customer view)
            
            **⚠️ Admin Only:** This endpoint should be restricted to admin users in production
            """,
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Categories retrieved successfully",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = CategoryDTO.class),
                    examples = @ExampleObject(
                        name = "Categories List",
                        value = """
                            [
                              {
                                "categoryId": 1,
                                "categoryCode": "SENIOR",
                                "categoryName": "Senior Citizen",
                                "benefitRate": 0.75,
                                "description": "Senior citizens aged 60 and above - Additional 0.75% interest"
                              },
                              {
                                "categoryId": 2,
                                "categoryCode": "JR",
                                "categoryName": "Junior Citizen",
                                "benefitRate": 0.50,
                                "description": "Minors below 18 years - Additional 0.50% interest"
                              },
                              {
                                "categoryId": 3,
                                "categoryCode": "GOLD",
                                "categoryName": "Gold Customer",
                                "benefitRate": 0.25,
                                "description": "Premium banking customers - Additional 0.25% interest"
                              },
                              {
                                "categoryId": 4,
                                "categoryCode": "EMP",
                                "categoryName": "Employee",
                                "benefitRate": 1.00,
                                "description": "Bank employees - Additional 1.00% interest"
                              }
                            ]
                            """
                    )
                )
            )
        }
    )
    @GetMapping("/categories")
    public ResponseEntity<List<CategoryDTO>> getAllCategories() {
        return ResponseEntity.ok(productRuleSyncService.getAllCategories());
    }
}
