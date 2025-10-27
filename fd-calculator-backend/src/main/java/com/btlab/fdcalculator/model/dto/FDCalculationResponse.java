package com.btlab.fdcalculator.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;

/**
 * Response DTO containing Fixed Deposit calculation results
 * Includes both cumulative and non-cumulative FD details
 * 
 * Currency-specific decimal precision:
 * - INR: 2 decimal places
 * - JPY: 0 decimal places (no decimals)
 * - AED: 3 decimal places
 * All amounts are rounded down
 */
@Schema(description = "Fixed Deposit calculation result with maturity details, interest rates, and periodic payout information. Amounts are formatted according to currency rules (INR: 2 decimals, JPY: 0 decimals, AED: 3 decimals, rounded down)")
public record FDCalculationResponse(
    
    @Schema(
        description = "Maturity value at the end of tenure. For cumulative FD: Principal + accumulated interest. For non-cumulative FD: Same as principal (since interest is paid out periodically). Formatted with currency-specific decimals (INR: 2, JPY: 0, AED: 3), rounded down",
        example = "164361.50",
        required = true
    )
    BigDecimal maturity_value,
    
    @Schema(
        description = "Maturity date in ISO format (YYYY-MM-DD)",
        example = "2030-10-10",
        required = true
    )
    String maturity_date,
    
    @Schema(
        description = "Annual Percentage Yield (APY) - The actual annual rate of return accounting for compounding. APY = (1 + r/n)^n - 1, where n = compounding periods per year. Always 4 decimal places, rounded down",
        example = "10.6508",
        required = true
    )
    BigDecimal apy,
    
    @Schema(
        description = "Effective interest rate after applying all category benefits. This is the nominal annual rate (base rate + category bonuses), not accounting for compounding. Always 4 decimal places, rounded down",
        example = "10.2500",
        required = true
    )
    BigDecimal effective_rate,
    
    @Schema(
        description = "Payout frequency for non-cumulative FDs. Null for cumulative FDs. MONTHLY = Interest paid 12 times/year, QUARTERLY = 4 times/year, YEARLY = 1 time/year",
        example = "QUARTERLY",
        allowableValues = {"MONTHLY", "QUARTERLY", "YEARLY"},
        nullable = true
    )
    String payout_freq,
    
    @Schema(
        description = "Interest amount paid per payout period for non-cumulative FDs. Null for cumulative FDs. Calculated using compound interest formula: P × [(1 + r/m)^n - 1], where m = compounding periods per year, n = compounding periods per payout. Formatted with currency-specific decimals (INR: 2, JPY: 0, AED: 3), rounded down",
        example = "5325.38",
        nullable = true
    )
    BigDecimal payout_amount,
    
    @Schema(
        description = "Unique calculation ID for retrieving this calculation later",
        example = "123",
        required = true
    )
    Long calc_id,
    
    @Schema(
        description = "Unique result ID for the calculation result record",
        example = "123",
        required = true
    )
    Long result_id,
    
    @Schema(
        description = "Primary customer category code used in calculation. Null if no category1 was applied",
        example = "SENIOR",
        allowableValues = {"SENIOR", "JR", "GOLD", "SILVER", "PLAT", "EMP", "DY", "MIN", "MAX", "MAXINT"},
        nullable = true
    )
    String category1_id,
    
    @Schema(
        description = "Secondary customer category code used in calculation. Null if no category2 was applied",
        example = "GOLD",
        allowableValues = {"SENIOR", "JR", "GOLD", "SILVER", "PLAT", "EMP", "DY", "MIN", "MAX", "MAXINT"},
        nullable = true
    )
    String category2_id,
    
    @Schema(
        description = "Product code used for this FD calculation",
        example = "FD001",
        required = true
    )
    String product_code,
    
    @Schema(
        description = "Principal amount deposited. Formatted with currency-specific decimals",
        example = "100000.00",
        required = true
    )
    BigDecimal principal_amount,
    
    @Schema(
        description = "Tenure value (numeric part of the tenure period)",
        example = "5",
        required = true
    )
    Integer tenure_value,
    
    @Schema(
        description = "Tenure unit (time period unit)",
        example = "YEARS",
        allowableValues = {"DAYS", "MONTHS", "YEARS"},
        required = true
    )
    String tenure_unit
) {}
