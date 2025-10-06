package com.btlab.fdcalculator.service.impl;

import com.btlab.fdcalculator.model.dto.FDCalculationRequest;
import com.btlab.fdcalculator.model.dto.FDCalculationResponse;
import com.btlab.fdcalculator.model.entity.*;
import com.btlab.fdcalculator.repository.*;
import com.btlab.fdcalculator.service.FDCalculatorService;
import com.btlab.fdcalculator.service.RateCacheService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.MathContext;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class FDCalculatorServiceImpl implements FDCalculatorService {

    private final CategoryRepository categoryRepo;
    private final FDCalculationInputRepository inputRepo;
    private final FDCalculationResultRepository resultRepo;
    private final RateCacheService rateCacheService;

    private static final BigDecimal MAX_EXTRA_PERCENT = new BigDecimal("2.00");

    @Override
    @Transactional
    public FDCalculationResponse calculate(FDCalculationRequest req) {
        Category cat1 = categoryRepo.findById(req.category1_id())
            .orElseThrow(() -> new IllegalArgumentException("Invalid category1_id"));
        Category cat2 = null;
        if (req.category2_id() != null) {
            cat2 = categoryRepo.findById(req.category2_id())
                .orElseThrow(() -> new IllegalArgumentException("Invalid category2_id"));
        }

        String productCode = req.product_code() == null ? "FD_STD" : req.product_code();
        BigDecimal baseRate = rateCacheService.getBaseRate(productCode);

        BigDecimal extra = cat1.getAdditionalPercentage();
        if (cat2 != null) extra = extra.add(cat2.getAdditionalPercentage());
        if (extra.compareTo(MAX_EXTRA_PERCENT) > 0) extra = MAX_EXTRA_PERCENT;

        BigDecimal effectiveRate = baseRate.add(extra);
        BigDecimal maturityValue;
        LocalDate maturityDate = calcMaturityDate(req.tenure_value(), req.tenure_unit());
        BigDecimal apy;

        if ("SIMPLE".equalsIgnoreCase(req.interest_type())) {
            maturityValue = simpleMaturity(
                req.principal_amount(), effectiveRate, req.tenure_value(), req.tenure_unit());
            apy = effectiveRate;
        } else {
            maturityValue = compoundMaturity(
                req.principal_amount(), effectiveRate, req.tenure_value(), req.tenure_unit(),
                req.compounding_frequency());
            apy = calcAPY(effectiveRate, req.compounding_frequency());
        }

        FDCalculationInput in = inputRepo.save(FDCalculationInput.builder()
            .currencyCode(req.currency_code())
            .principalAmount(req.principal_amount())
            .tenureValue(req.tenure_value())
            .tenureUnit(req.tenure_unit())
            .interestType(req.interest_type())
            .compoundingFrequency(req.compounding_frequency())
            .category1(cat1)
            .category2(cat2)
            .requestTimestamp(LocalDateTime.now())
            .build());

        FDCalculationResult res = resultRepo.save(FDCalculationResult.builder()
            .calc(in)
            .maturityValue(maturityValue)
            .maturityDate(maturityDate)
            .apy(apy)
            .effectiveRate(effectiveRate)
            .build());

        return new FDCalculationResponse(
            res.getMaturityValue(),
            res.getMaturityDate().toString(),
            res.getApy(),
            res.getEffectiveRate(),
            in.getCalcId(),
            res.getResultId()
        );
    }

    @Override
    public FDCalculationResponse getByCalcId(Long calcId) {
        FDCalculationResult res = resultRepo.findByCalc_CalcId(calcId);
        if (res == null) throw new IllegalArgumentException("Calculation not found");
        return new FDCalculationResponse(
            res.getMaturityValue(),
            res.getMaturityDate().toString(),
            res.getApy(),
            res.getEffectiveRate(),
            res.getCalc().getCalcId(),
            res.getResultId()
        );
    }

    private LocalDate calcMaturityDate(int tenureValue, String tenureUnit) {
        LocalDate today = LocalDate.now();
        return switch (tenureUnit.toUpperCase()) {
            case "DAYS" -> today.plusDays(tenureValue);
            case "MONTHS" -> today.plusMonths(tenureValue);
            case "YEARS" -> today.plusYears(tenureValue);
            default -> throw new IllegalArgumentException("Invalid tenure_unit");
        };
    }

    private BigDecimal simpleMaturity(BigDecimal principal, BigDecimal ratePct, int tenure, String unit) {
        BigDecimal years = toYears(tenure, unit);
        BigDecimal r = ratePct.movePointLeft(2);
        return principal.add(principal.multiply(r).multiply(years)).setScale(4, BigDecimal.ROUND_HALF_UP);
    }

    private BigDecimal compoundMaturity(BigDecimal principal, BigDecimal ratePct, int tenure, String unit, String freq) {
        BigDecimal years = toYears(tenure, unit);
        int n = switch ((freq == null ? "YEARLY" : freq).toUpperCase()) {
            case "DAILY" -> 365;
            case "MONTHLY" -> 12;
            case "QUARTERLY" -> 4;
            case "YEARLY" -> 1;
            default -> throw new IllegalArgumentException("Invalid compounding_frequency");
        };
        BigDecimal r = ratePct.movePointLeft(2);
        double factor = Math.pow(1.0 + r.doubleValue() / n, n * years.doubleValue());
        return principal.multiply(new BigDecimal(factor, MathContext.DECIMAL64)).setScale(4, BigDecimal.ROUND_HALF_UP);
    }

    private BigDecimal calcAPY(BigDecimal ratePct, String freq) {
        int n = switch ((freq == null ? "YEARLY" : freq).toUpperCase()) {
            case "DAILY" -> 365; case "MONTHLY" -> 12; case "QUARTERLY" -> 4; case "YEARLY" -> 1;
            default -> 1;
        };
        BigDecimal r = ratePct.movePointLeft(2);
        double apy = Math.pow(1 + r.doubleValue() / n, n) - 1;
        return new BigDecimal(apy).movePointRight(2).setScale(4, BigDecimal.ROUND_HALF_UP);
    }

    private BigDecimal toYears(int tenure, String unit) {
        return switch (unit.toUpperCase()) {
            case "DAYS" -> new BigDecimal(tenure).divide(new BigDecimal("365"), MathContext.DECIMAL64);
            case "MONTHS" -> new BigDecimal(tenure).divide(new BigDecimal("12"), MathContext.DECIMAL64);
            case "YEARS" -> new BigDecimal(tenure);
            default -> throw new IllegalArgumentException("Invalid tenure_unit");
        };
    }
}
