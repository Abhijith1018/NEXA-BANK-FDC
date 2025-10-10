package com.btlab.fdcalculator.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;

/**
 * DTO representing customer category with benefit information
 */
@Schema(description = "Customer category information with benefit rates")
public record CategoryDTO(
    
    @Schema(
        description = "Unique identifier for the category",
        example = "1",
        required = true
    )
    Long category_id,
    
    @Schema(
        description = "Display name of the category",
        example = "Senior Citizen",
        required = true
    )
    String category_name,
    
    @Schema(
        description = "Additional interest percentage benefit for this category (in percentage points)",
        example = "0.75",
        required = true
    )
    BigDecimal additional_percentage
) {}
