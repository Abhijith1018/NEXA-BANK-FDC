package com.btlab.fdcalculator.controller;

import com.btlab.fdcalculator.model.dto.CategoryDTO;
import com.btlab.fdcalculator.model.entity.RateCache;
import com.btlab.fdcalculator.repository.RateCacheRepository;
import com.btlab.fdcalculator.service.CategoryService;
import com.btlab.fdcalculator.service.RateCacheService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * Controller for reference data and configuration
 * Provides lookup data for FD calculations and rate cache management
 */
@RestController
@RequestMapping("/api/fd")
@RequiredArgsConstructor
@Tag(name = "Reference Data", description = "Reference data endpoints for categories, currencies, compounding options, and rate cache management")
public class ReferenceDataController {

    private final CategoryService categoryService;
    private final RateCacheService rateCacheService;
    private final RateCacheRepository rateCacheRepository;

    @Operation(
        summary = "Get all customer categories",
        description = """
            Retrieve all available customer categories and their benefit rates.
            
            **Categories include:**
            - **SENIOR**: Senior Citizen - Additional 0.75% interest
            - **JR**: Junior Citizen - Additional 0.50% interest
            - **GOLD**: Gold Customer - Additional 0.25% interest
            - **SILVER**: Silver Customer - Additional 0.15% interest
            - **PLAT**: Platinum Customer - Additional 0.35% interest
            - **EMP**: Employee - Additional 1.00% interest
            - **DY**: Divyang (Differently-abled) - Additional 1.25% interest
            
            **Use Case:**
            - Populate dropdown for customer category selection
            - Display available benefits to customers
            - Calculate total interest rate with category benefits
            
            **Note:** Categories can be combined (category1_id + category2_id) for cumulative benefits
            """,
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "List of categories retrieved successfully",
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
                                "description": "Senior citizens aged 60 and above"
                              },
                              {
                                "categoryId": 2,
                                "categoryCode": "GOLD",
                                "categoryName": "Gold Customer",
                                "benefitRate": 0.25,
                                "description": "Premium banking customers"
                              }
                            ]
                            """
                    )
                )
            )
        }
    )
    @GetMapping("/categories")
    public List<CategoryDTO> categories() {
        return categoryService.getCategories();
    }

    @Operation(
        summary = "Get supported currencies",
        description = """
            Retrieve list of supported currency codes for FD calculations.
            
            **Currently Supported:**
            - **INR**: Indian Rupee
            - **JPY**: Japanese Yen
            - **AED**: UAE Dirham
            
            **Use Case:**
            - Populate currency dropdown in UI
            - Validate currency input
            - Multi-currency FD support
            """,
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "List of currency codes",
                content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(
                        name = "Currency Codes",
                        value = "[\"INR\", \"JPY\", \"AED\"]"
                    )
                )
            )
        }
    )
    @GetMapping("/currencies")
    public List<String> currencies() {
        return List.of("INR","JPY","AED");
    }

    @Operation(
        summary = "Get compounding frequency options",
        description = """
            Retrieve available compounding frequency options for compound interest calculations.
            
            **Options:**
            - **DAILY**: Interest compounded 365 times per year (highest APY)
            - **MONTHLY**: Interest compounded 12 times per year
            - **QUARTERLY**: Interest compounded 4 times per year (most common)
            - **YEARLY**: Interest compounded once per year (lowest APY)
            
            **Impact on APY:**
            More frequent compounding = Higher APY
            
            **Example:**
            For 10% annual rate:
            - Daily: APY ≈ 10.52%
            - Monthly: APY ≈ 10.47%
            - Quarterly: APY ≈ 10.38%
            - Yearly: APY = 10.00%
            
            **Use Case:**
            - Populate compounding frequency dropdown
            - Show available options to customers
            - Calculate APY based on frequency
            """,
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "List of compounding frequencies",
                content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(
                        name = "Compounding Options",
                        value = "[\"DAILY\", \"MONTHLY\", \"QUARTERLY\", \"YEARLY\"]"
                    )
                )
            )
        }
    )
    @GetMapping("/compounding-options")
    public List<String> compounding() {
        return List.of("DAILY","MONTHLY","QUARTERLY","YEARLY");
    }

    @Operation(
        summary = "Refresh rate cache",
        description = """
            Manually refresh the cached interest rates for a specific product code.
            
            **Purpose:**
            - Interest rates are cached for performance
            - This endpoint forces a refresh from Product & Pricing API
            - Use when rates are updated in the pricing system
            
            **Process:**
            1. Calls Product & Pricing API
            2. Fetches latest base rate for the product
            3. Updates cache in database
            4. Returns confirmation message
            
            **When to Use:**
            - After rate changes in Product & Pricing API
            - When cache seems stale
            - For testing with new rates
            
            **Note:** Automatic refresh happens periodically via scheduler
            """,
        parameters = {
            @Parameter(
                name = "productCode",
                description = "Product code to refresh rates for (e.g., FD001)",
                required = true,
                example = "FD001"
            )
        }
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Cache refreshed successfully",
            content = @Content(
                mediaType = "text/plain",
                examples = @ExampleObject(
                    name = "Success Message",
                    value = "Refreshed FD001"
                )
            )
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Failed to refresh cache (Product API unavailable)",
            content = @Content(mediaType = "text/plain")
        )
    })
    @PostMapping("/rate-cache/refresh")
    public String refresh(
        @RequestParam 
        @Parameter(description = "Product code", example = "FD001") 
        String productCode
    ) {
        rateCacheService.refreshRate(productCode);
        return "Refreshed " + productCode;
    }

    @Operation(
        summary = "Get cached base rate",
        description = """
            Retrieve the currently cached base interest rate for a product.
            
            **Purpose:**
            - View current cached rate without recalculating
            - Check if cache is populated
            - Debugging rate-related issues
            
            **Returns:**
            - Base interest rate (before category benefits)
            - Null if product not found in cache
            
            **Note:** 
            - This is the base rate only
            - Actual customer rate = base rate + category benefits
            - Use /calculate endpoint for full rate calculation
            
            **Example:**
            - Cached base rate: 7.5%
            - Customer category: SENIOR (+0.75%)
            - Effective rate: 8.25%
            """,
        parameters = {
            @Parameter(
                name = "productCode",
                description = "Product code to get rate for",
                required = true,
                example = "FD001"
            )
        }
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Base rate retrieved (or null if not cached)",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(type = "number", format = "decimal"),
                examples = {
                    @ExampleObject(
                        name = "Rate Found",
                        value = "7.50"
                    ),
                    @ExampleObject(
                        name = "Rate Not Found",
                        value = "null"
                    )
                }
            )
        )
    })
    @GetMapping("/rate-cache/{productCode}")
    public BigDecimal getRate(
        @PathVariable 
        @Parameter(description = "Product code", example = "FD001") 
        String productCode
    ) {
        return rateCacheRepository.findById(productCode)
            .map(RateCache::getBaseRate)
            .orElse(null);
    }
}
