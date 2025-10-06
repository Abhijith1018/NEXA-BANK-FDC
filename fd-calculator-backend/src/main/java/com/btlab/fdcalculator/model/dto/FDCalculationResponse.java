package com.btlab.fdcalculator.model.dto;

import java.math.BigDecimal;

public record FDCalculationResponse(
    BigDecimal maturity_value,
    String maturity_date,
    BigDecimal apy,
    BigDecimal effective_rate,
    Long calc_id,
    Long result_id
) {}
