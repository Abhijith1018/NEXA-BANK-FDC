package com.btlab.fdcalculator.model.dto;

import java.math.BigDecimal;

public record FDCalculationResponse(
    BigDecimal maturity_value,
    String maturity_date,
    BigDecimal apy,
    BigDecimal effective_rate,
    String payout_freq,         // MONTHLY, QUARTERLY, YEARLY (null if cumulative)
    BigDecimal payout_amount,   // Interest paid per period (null if cumulative)
    Long calc_id,
    Long result_id
) {}
