package com.btlab.fdcalculator.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for Product Details from Product & Pricing API
 * Contains product configuration including interest type and compounding frequency
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductDetailsDTO {
    private String productId;
    private String productCode;
    private String productName;
    private String productType;
    private String currency;
    private String status;
    private String interestType;
    private String compoundingFrequency;
    // Add other fields as needed from the API response
}
