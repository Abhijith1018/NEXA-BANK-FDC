package com.btlab.fdcalculator.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;

/**
 * Response DTO containing Fixed Deposit calculation results
 * Includes both cumulative and non-cumulative FD details
 */
@Schema(description = "Fixed Deposit calculation result with maturity details, interest rates, and periodic payout information")
public record FDCalculationResponse(
    
    @Schema(
        description = "Maturity value at the end of tenure. For cumulative FD: Principal + accumulated interest. For non-cumulative FD: Same as principal (since interest is paid out periodically)",
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
        description = "Annual Percentage Yield (APY) - The actual annual rate of return accounting for compounding. APY = (1 + r/n)^n - 1, where n = compounding periods per year. For example, 10.25% quarterly compounded = 10.65% APY",
        example = "10.6508",
        required = true
    )
    BigDecimal apy,
    
    @Schema(
        description = "Effective interest rate after applying all category benefits. This is the nominal annual rate (base rate + category bonuses), not accounting for compounding",
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
        description = "Interest amount paid per payout period for non-cumulative FDs. Null for cumulative FDs. Calculated using compound interest formula: P × [(1 + r/m)^n - 1], where m = compounding periods per year, n = compounding periods per payout",
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
    Long result_id
) {}
