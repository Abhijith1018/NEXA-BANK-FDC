package com.btlab.fdcalculator.model.dto;


//import org.antlr.v4.runtime.misc.NotNull;
//import org.hibernate.annotations.processing.Pattern;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;


import java.math.BigDecimal;

public record FDCalculationRequest(
    @Pattern(regexp = "INR|JPY|AED") String currency_code,
    @NotNull @DecimalMin("0.01") BigDecimal principal_amount,
    @NotNull @Min(1) Integer tenure_value,
    @Pattern(regexp = "DAYS|MONTHS|YEARS") String tenure_unit,
    @Pattern(regexp = "SIMPLE|COMPOUND") String interest_type,
    String compounding_frequency,
    @NotNull Long category1_id,
    Long category2_id,
    String product_code
) {}
