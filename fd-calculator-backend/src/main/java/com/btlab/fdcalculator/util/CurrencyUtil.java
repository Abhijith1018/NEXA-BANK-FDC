package com.btlab.fdcalculator.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Utility class for currency-specific formatting and rounding
 */
public class CurrencyUtil {
    
    /**
     * Get the number of decimal places for a given currency
     * @param currencyCode The currency code (INR, JPY, AED)
     * @return Number of decimal places
     */
    public static int getDecimalPlaces(String currencyCode) {
        if (currencyCode == null) {
            return 2; // Default to 2 decimals
        }
        
        return switch (currencyCode.toUpperCase()) {
            case "JPY" -> 0;  // Japanese Yen has no decimals
            case "AED" -> 3;  // UAE Dirham has 3 decimals
            case "INR" -> 2;  // Indian Rupee has 2 decimals
            default -> 2;     // Default to 2 decimals
        };
    }
    
    /**
     * Format amount according to currency rules (round down)
     * @param amount The amount to format
     * @param currencyCode The currency code
     * @return Formatted amount with appropriate decimal places, rounded down
     */
    public static BigDecimal formatAmount(BigDecimal amount, String currencyCode) {
        if (amount == null) {
            return null;
        }
        
        int decimalPlaces = getDecimalPlaces(currencyCode);
        return amount.setScale(decimalPlaces, RoundingMode.DOWN);
    }
    
    /**
     * Format percentage/rate values (always 4 decimal places, rounded down)
     * @param rate The rate to format
     * @return Formatted rate with 4 decimal places, rounded down
     */
    public static BigDecimal formatRate(BigDecimal rate) {
        if (rate == null) {
            return null;
        }
        return rate.setScale(4, RoundingMode.DOWN);
    }
}
