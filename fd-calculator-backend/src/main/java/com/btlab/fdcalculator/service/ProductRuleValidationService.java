package com.btlab.fdcalculator.service;

import java.math.BigDecimal;

public interface ProductRuleValidationService {
    /**
     * Validate if the principal amount is within the allowed range for the product
     * @param productCode The product code (e.g., "FD001")
     * @param amount The principal amount to validate
     * @throws IllegalArgumentException if amount is outside the allowed range
     */
    void validateAmount(String productCode, BigDecimal amount);
    
    /**
     * Get the minimum amount allowed for the product
     */
    BigDecimal getMinimumAmount(String productCode);
    
    /**
     * Get the maximum amount allowed for the product
     */
    BigDecimal getMaximumAmount(String productCode);
    
    /**
     * Get the maximum excess interest allowed for the product
     */
    BigDecimal getMaximumExcessInterest(String productCode);
}
