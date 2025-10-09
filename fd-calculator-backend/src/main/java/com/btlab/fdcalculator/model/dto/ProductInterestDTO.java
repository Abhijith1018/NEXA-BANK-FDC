package com.btlab.fdcalculator.model.dto;

import java.math.BigDecimal;

public record ProductInterestDTO(
        String rateId,
        String rateCode,         // e.g., "INT12M001", "INT24M001"
        int termInMonths,
        BigDecimal rateCumulative,
        BigDecimal rateNonCumulativeMonthly,
        BigDecimal rateNonCumulativeQuarterly,
        BigDecimal rateNonCumulativeYearly
) {}