package com.btlab.fdcalculator.model.dto;

import java.math.BigDecimal;

public record ProductInterestDTO(
        String rateId,
        int termInMonths,
        BigDecimal rateCumulative,
        BigDecimal rateNonCumulativeMonthly,
        BigDecimal rateNonCumulativeQuarterly,
        BigDecimal rateNonCumulativeYearly
) {}