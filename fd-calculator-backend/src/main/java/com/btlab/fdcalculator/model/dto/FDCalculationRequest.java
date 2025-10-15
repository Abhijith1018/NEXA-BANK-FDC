package com.btlab.fdcalculator.model.dto;


import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.math.BigDecimal;

/**
 * Request DTO for Fixed Deposit calculations
 * Supports both cumulative and non-cumulative FD types
 */
@Schema(description = "Fixed Deposit calculation request containing all parameters needed for FD calculation")
public record FDCalculationRequest(
    
    @Schema(
        description = "Currency code for the deposit",
        example = "INR",
        allowableValues = {"INR", "JPY", "AED"},
        required = false,
        defaultValue = "INR"
    )
    @Pattern(regexp = "INR|JPY|AED") 
    String currency_code,
    
    @Schema(
        description = "Principal amount to be deposited",
        example = "100000",
        minimum = "0.01",
        required = true
    )
    @NotNull 
    @DecimalMin("0.01") 
    BigDecimal principal_amount,
    
    @Schema(
        description = "Tenure value (numeric part of the tenure period)",
        example = "5",
        minimum = "1",
        required = true
    )
    @NotNull 
    @Min(1) 
    Integer tenure_value,
    
    @Schema(
        description = "Tenure unit (time period unit)",
        example = "YEARS",
        allowableValues = {"DAYS", "MONTHS", "YEARS"},
        required = true
    )
    @Pattern(regexp = "DAYS|MONTHS|YEARS") 
    String tenure_unit,
    
    @Schema(
        description = "Type of interest calculation. SIMPLE = Simple interest (rarely used), COMPOUND = Compound interest (most common). If not provided, will be fetched from Product & Pricing API",
        example = "COMPOUND",
        allowableValues = {"SIMPLE", "COMPOUND"},
        required = false
    )
    @Pattern(regexp = "SIMPLE|COMPOUND") 
    String interest_type,
    
    @Schema(
        description = "Frequency of compounding. Required for COMPOUND interest type. Options: DAILY (365 times/year), MONTHLY (12 times/year), QUARTERLY (4 times/year), YEARLY (1 time/year). If not provided, will be fetched from Product & Pricing API",
        example = "QUARTERLY",
        allowableValues = {"DAILY", "MONTHLY", "QUARTERLY", "YEARLY"},
        required = false
    )
    String compounding_frequency,
    
    @Schema(
        description = "Primary customer category code for additional benefits. SENIOR=Senior Citizen (+0.75%), JR=Junior (+0.50%), GOLD=Gold Customer (+0.25%), SILVER=Silver (+0.15%), PLAT=Platinum (+0.35%), EMP=Employee (+1.00%), DY=Divyang (+1.25%)",
        example = "SENIOR",
        allowableValues = {"SENIOR", "JR", "GOLD", "SILVER", "PLAT", "EMP", "DY", "MIN", "MAX", "MAXINT"},
        required = false
    )
    String category1_id,
    
    @Schema(
        description = "Secondary customer category code for additional benefits (can stack with category1)",
        example = "GOLD",
        allowableValues = {"SENIOR", "JR", "GOLD", "SILVER", "PLAT", "EMP", "DY", "MIN", "MAX", "MAXINT"},
        required = false
    )
    String category2_id,
    
    @Schema(
        description = "FD type: true = Cumulative FD (interest reinvested, paid at maturity), false = Non-Cumulative FD (interest paid periodically)",
        example = "true",
        required = false,
        defaultValue = "true"
    )
    Boolean cumulative,
    
    @Schema(
        description = "Payout frequency for non-cumulative FDs. Required when cumulative=false. Ignored when cumulative=true. MONTHLY=12 times/year, QUARTERLY=4 times/year, YEARLY=1 time/year",
        example = "QUARTERLY",
        allowableValues = {"MONTHLY", "QUARTERLY", "YEARLY"},
        required = false
    )
    @Pattern(regexp = "MONTHLY|QUARTERLY|YEARLY") 
    String payout_freq,
    
    @Schema(
        description = "Product code for the FD scheme. Used to fetch product-specific rules and base interest rates",
        example = "FD001",
        required = true
    )
    String product_code
) {}
