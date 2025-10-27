package com.btlab.fdcalculator.service.impl;

import com.btlab.fdcalculator.client.PricingApiClient;
import com.btlab.fdcalculator.model.dto.FDCalculationRequest;
import com.btlab.fdcalculator.model.dto.FDCalculationResponse;
import com.btlab.fdcalculator.model.dto.ProductDetailsDTO;
import com.btlab.fdcalculator.model.dto.ProductRuleDTO;
import com.btlab.fdcalculator.model.entity.*;
import com.btlab.fdcalculator.repository.*;
import com.btlab.fdcalculator.service.FDCalculatorService;
import com.btlab.fdcalculator.service.ProductRuleValidationService;
import com.btlab.fdcalculator.service.RateCacheService;
import com.btlab.fdcalculator.util.CurrencyUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class FDCalculatorServiceImpl implements FDCalculatorService {

    private final FDCalculationInputRepository inputRepo;
    private final FDCalculationResultRepository resultRepo;
    private final RateCacheService rateCacheService;
    private final ProductRuleValidationService productRuleValidationService;
    private final PricingApiClient pricingApiClient;

    @Override
    @Transactional
    public FDCalculationResponse calculate(FDCalculationRequest req) {
        String productCode = req.product_code() == null ? "FD001" : req.product_code();
        
        // Fetch product details to get interestType and compoundingFrequency
        ProductDetailsDTO productDetails = pricingApiClient.getProductDetails(productCode);
        log.info("Fetched product details for {}: interestType={}, compoundingFrequency={}", 
            productCode, productDetails.getInterestType(), productDetails.getCompoundingFrequency());
        
        // Use values from product details if not provided in request
        String interestType = req.interest_type() != null ? req.interest_type() : productDetails.getInterestType();
        String compoundingFrequency = req.compounding_frequency() != null ? req.compounding_frequency() : productDetails.getCompoundingFrequency();
        
        if (interestType == null) {
            throw new IllegalArgumentException("Interest type not found in request or product details");
        }
        
        log.info("Using interestType={}, compoundingFrequency={}", interestType, compoundingFrequency);
        
        // Validate the principal amount against product rules
        productRuleValidationService.validateAmount(productCode, req.principal_amount());
        
        // Extract the product suffix (last 3 digits) for rule code construction
        String productSuffix = extractProductSuffix(productCode);
        
        // Calculate tenure in months
        int tenureInMonths = calculateTenureInMonths(req.tenure_value(), req.tenure_unit());
        
        // Get base rate from Product & Pricing API based on tenure
        BigDecimal baseRate = getBaseRateFromApi(productCode, productSuffix, tenureInMonths, 
            req.cumulative(), req.payout_freq(), compoundingFrequency);
        log.info("Base rate from API: {}%", baseRate);
        
        // Fetch category benefits from Product & Pricing API
        BigDecimal extra = BigDecimal.ZERO;
        String category1RuleCode = null;
        String category2RuleCode = null;
        
        if (req.category1_id() != null && !req.category1_id().isBlank()) {
            category1RuleCode = constructRuleCode(req.category1_id(), productSuffix);
            BigDecimal cat1Benefit = getCategoryBenefit(productCode, category1RuleCode, req.category1_id());
            extra = extra.add(cat1Benefit);
            log.info("Category 1 ({}) benefit: {}%", req.category1_id(), cat1Benefit);
        }
        
        if (req.category2_id() != null && !req.category2_id().isBlank()) {
            category2RuleCode = constructRuleCode(req.category2_id(), productSuffix);
            BigDecimal cat2Benefit = getCategoryBenefit(productCode, category2RuleCode, req.category2_id());
            extra = extra.add(cat2Benefit);
            log.info("Category 2 ({}) benefit: {}%", req.category2_id(), cat2Benefit);
        }
        
        // Get the maximum excess interest from product rules
        BigDecimal maxExtraPercent = productRuleValidationService.getMaximumExcessInterest(productCode);
        log.info("Total extra before cap: {}%, Max allowed: {}%", extra, maxExtraPercent);
        
        if (extra.compareTo(maxExtraPercent) > 0) {
            log.warn("Extra interest {}% exceeds maximum {}%. Capping at maximum.", extra, maxExtraPercent);
            extra = maxExtraPercent;
        }
        
        BigDecimal effectiveRate = baseRate.add(extra);
        
        log.info("Base rate: {}%, Extra: {}%, Effective rate: {}%", baseRate, extra, effectiveRate);
        
        BigDecimal maturityValue;
        LocalDate maturityDate = calcMaturityDate(req.tenure_value(), req.tenure_unit());
        BigDecimal apy;
        String payoutFreq = null;
        BigDecimal payoutAmount = null;
        
        // Check if this is a non-cumulative FD
        boolean isNonCumulative = req.cumulative() != null && !req.cumulative();
        
        if (isNonCumulative) {
            // Non-cumulative: Interest paid out periodically, maturity = principal only
            payoutFreq = req.payout_freq() != null ? req.payout_freq() : req.compounding_frequency();
            if (payoutFreq == null) payoutFreq = "YEARLY";
            
            // Calculate periodic payout amount with compounding
            payoutAmount = calculatePeriodicPayoutWithCompounding(
                req.principal_amount(), effectiveRate, payoutFreq, 
                compoundingFrequency);
            
            // For non-cumulative, maturity value is principal only
            maturityValue = req.principal_amount();
            
            // Calculate APY based on compounding frequency
            // Even though interest is paid out, it still compounds during the payout period
            if (compoundingFrequency != null && !"SIMPLE".equalsIgnoreCase(interestType)) {
                apy = calcAPY(effectiveRate, compoundingFrequency);
            } else {
                apy = effectiveRate; // Simple interest or no compounding
            }
            
            log.info("Non-cumulative FD: Payout freq={}, Compounding freq={}, Payout amount={}, APY={}, Maturity=Principal only", 
                payoutFreq, compoundingFrequency, payoutAmount, apy);
        } else {
            // Cumulative: Interest compounded and paid at maturity
            if ("SIMPLE".equalsIgnoreCase(interestType)) {
                maturityValue = simpleMaturity(
                    req.principal_amount(), effectiveRate, req.tenure_value(), req.tenure_unit());
                apy = effectiveRate;
            } else {
                maturityValue = compoundMaturity(
                    req.principal_amount(), effectiveRate, req.tenure_value(), req.tenure_unit(),
                    compoundingFrequency);
                apy = calcAPY(effectiveRate, compoundingFrequency);
            }
            
            log.info("Cumulative FD: Maturity value={}", maturityValue);
        }

        // Get currency code for formatting
        String currencyCode = req.currency_code() != null ? req.currency_code() : "INR";
        
        // Apply currency-specific formatting (round down)
        maturityValue = CurrencyUtil.formatAmount(maturityValue, currencyCode);
        if (payoutAmount != null) {
            payoutAmount = CurrencyUtil.formatAmount(payoutAmount, currencyCode);
        }
        
        // Format rates (4 decimals, round down)
        effectiveRate = CurrencyUtil.formatRate(effectiveRate);
        apy = CurrencyUtil.formatRate(apy);

        FDCalculationInput in = inputRepo.save(FDCalculationInput.builder()
            .currencyCode(currencyCode)
            .principalAmount(req.principal_amount())
            .tenureValue(req.tenure_value())
            .tenureUnit(req.tenure_unit())
            .interestType(interestType)
            .compoundingFrequency(compoundingFrequency)
            .category1Code(req.category1_id())
            .category2Code(req.category2_id())
            .productCode(productCode)
            .requestTimestamp(LocalDateTime.now())
            .build());

        FDCalculationResult res = resultRepo.save(FDCalculationResult.builder()
            .calc(in)
            .maturityValue(maturityValue)
            .maturityDate(maturityDate)
            .apy(apy)
            .effectiveRate(effectiveRate)
            .payoutFreq(payoutFreq)
            .payoutAmount(payoutAmount)
            .build());

        return new FDCalculationResponse(
            res.getMaturityValue(),
            res.getMaturityDate().toString(),
            res.getApy(),
            res.getEffectiveRate(),
            res.getPayoutFreq(),
            res.getPayoutAmount(),
            in.getCalcId(),
            res.getResultId(),
            in.getCategory1Code(),
            in.getCategory2Code(),
            in.getProductCode(),
            in.getPrincipalAmount(),
            in.getTenureValue(),
            in.getTenureUnit()
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
            res.getPayoutFreq(),
            res.getPayoutAmount(),
            res.getCalc().getCalcId(),
            res.getResultId(),
            res.getCalc().getCategory1Code(),
            res.getCalc().getCategory2Code(),
            res.getCalc().getProductCode(),
            res.getCalc().getPrincipalAmount(),
            res.getCalc().getTenureValue(),
            res.getCalc().getTenureUnit()
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
    
    /**
     * Calculate periodic payout amount for non-cumulative FDs with compounding
     * 
     * For non-cumulative FDs:
     * - Interest is compounded based on compounding_frequency
     * - Accumulated interest is paid out based on payout_freq
     * 
     * Example: compounding_frequency = QUARTERLY, payout_freq = YEARLY
     * - Interest compounds 4 times per year
     * - Accumulated interest for the year is paid out yearly
     * 
     * Formula: Payout = Principal × [(1 + r/n)^n - 1]
     * where:
     * - r = annual rate (as decimal)
     * - n = number of compounding periods per payout period
     */
    private BigDecimal calculatePeriodicPayoutWithCompounding(
            BigDecimal principal, 
            BigDecimal ratePct, 
            String payoutFreq,
            String compoundingFreq) {
        
        // Convert rate from percentage to decimal
        BigDecimal rate = ratePct.movePointLeft(2);
        
        // If compounding frequency is null, use payout frequency
        if (compoundingFreq == null) compoundingFreq = payoutFreq;
        
        // Get compounding periods per year
        int compoundingPeriodsPerYear = switch (compoundingFreq.toUpperCase()) {
            case "DAILY" -> 365;
            case "MONTHLY" -> 12;
            case "QUARTERLY" -> 4;
            case "YEARLY" -> 1;
            default -> 4; // Default to quarterly
        };
        
        // Get payout periods per year
        int payoutPeriodsPerYear = switch (payoutFreq.toUpperCase()) {
            case "MONTHLY" -> 12;
            case "QUARTERLY" -> 4;
            case "YEARLY" -> 1;
            default -> 1; // Default to yearly
        };
        
        // Calculate number of compounding periods per payout period
        // Example: If compounding is QUARTERLY (4/year) and payout is YEARLY (1/year)
        //          then n = 4/1 = 4 (compounds 4 times per payout)
        int n = compoundingPeriodsPerYear / payoutPeriodsPerYear;
        
        if (n < 1) {
            // Payout frequency is more frequent than compounding
            // Example: compounding YEARLY but payout QUARTERLY
            // In this case, use simple interest calculation
            n = 1;
            log.warn("Payout frequency ({}) is more frequent than compounding frequency ({}). Using simple interest calculation.", 
                     payoutFreq, compoundingFreq);
        }
        
        // Calculate compound interest for one payout period
        // Formula: A = P × [(1 + r/m)^n - 1]
        // where m = compounding periods per year, n = compounding periods per payout
        double ratePerCompoundingPeriod = rate.doubleValue() / compoundingPeriodsPerYear;
        double compoundFactor = Math.pow(1.0 + ratePerCompoundingPeriod, n);
        double interestFactor = compoundFactor - 1.0;
        
        BigDecimal payoutAmount = principal.multiply(
            new BigDecimal(interestFactor, MathContext.DECIMAL64)
        );
        
        log.info("Non-cumulative payout calculation: Principal={}, Rate={}%, " +
                 "Compounding freq={} ({}/year), Payout freq={} ({}/year), " +
                 "Compounds per payout={}, Compound factor={}, Payout amount per period={}", 
                 principal, ratePct, compoundingFreq, compoundingPeriodsPerYear, 
                 payoutFreq, payoutPeriodsPerYear, n, compoundFactor, payoutAmount);
        
        return payoutAmount.setScale(4, RoundingMode.HALF_UP);
    }
    
    /**
     * Extract product suffix (last 3 digits) from product code
     * e.g., "FD001" -> "001"
     */
    private String extractProductSuffix(String productCode) {
        if (productCode != null && productCode.length() >= 3) {
            return productCode.substring(productCode.length() - 3);
        }
        return "001"; // Default suffix
    }
    
    /**
     * Construct rule code from category code and product suffix
     * e.g., "SENIOR" + "001" -> "SR001", "GOLD" + "001" -> "GOLD001"
     */
    private String constructRuleCode(String categoryCode, String productSuffix) {
        String upperCategoryCode = categoryCode.toUpperCase();
        
        // Map common category names to rule code prefixes
        String prefix = switch (upperCategoryCode) {
            case "SENIOR", "SENIOR_CITIZEN", "SR" -> "SR";
            case "JUNIOR", "JR" -> "JR";
            case "DIGI_YOUTH", "DY" -> "DY";
            case "GOLD" -> "GOLD";
            case "SILVER", "SIL" -> "SIL";
            case "PLATINUM", "PLAT" -> "PLAT";
            case "EMPLOYEE", "EMP" -> "EMP";
            default -> upperCategoryCode; // Use as-is if not a known category
        };
        
        return prefix + productSuffix;
    }
    
    /**
     * Fetch category benefit from Product & Pricing API
     */
    private BigDecimal getCategoryBenefit(String productCode, String ruleCode, String categoryName) {
        try {
            ProductRuleDTO rule = pricingApiClient.getRuleByCode(productCode, ruleCode);
            
            if (rule == null) {
                log.warn("No rule found for category: {} (rule code: {}). Using 0% benefit.", 
                    categoryName, ruleCode);
                return BigDecimal.ZERO;
            }
            
            log.info("Found rule: {} with value: {}", rule.ruleName(), rule.ruleValue());
            return new BigDecimal(rule.ruleValue());
            
        } catch (Exception e) {
            log.error("Error fetching category benefit for {} (rule code: {}): {}", 
                categoryName, ruleCode, e.getMessage());
            return BigDecimal.ZERO;
        }
    }
    
    /**
     * Calculate tenure in months from tenure value and unit
     */
    private int calculateTenureInMonths(int tenureValue, String tenureUnit) {
        return switch (tenureUnit.toUpperCase()) {
            case "DAYS" -> (int) Math.ceil(tenureValue / 30.0); // Approximate days to months
            case "MONTHS" -> tenureValue;
            case "YEARS" -> tenureValue * 12;
            default -> throw new IllegalArgumentException("Invalid tenure_unit: " + tenureUnit);
        };
    }
    
    /**
     * Construct interest rate code based on tenure
     * 0-12 months -> INT12M001
     * 12-24 months -> INT24M001
     * 24-36 months -> INT36M001
     * 36-60 months -> INT60M001
     * 60+ months -> INT60M001
     */
    private String constructRateCode(int tenureInMonths, String productSuffix) {
        String tenurePart;
        if (tenureInMonths <= 12) {
            tenurePart = "12M";
        } else if (tenureInMonths <= 24) {
            tenurePart = "24M";
        } else if (tenureInMonths <= 36) {
            tenurePart = "36M";
        } else {
            tenurePart = "60M"; // 36+ months use 60M rates
        }
        
        return "INT" + tenurePart + productSuffix;
    }
    
    /**
     * Get base rate from Product & Pricing API based on tenure and cumulative flag
     */
    private BigDecimal getBaseRateFromApi(String productCode, String productSuffix, 
                                          int tenureInMonths, Boolean cumulative, 
                                          String payoutFreq, String compoundingFreq) {
        try {
            // Construct rate code based on tenure
            String rateCode = constructRateCode(tenureInMonths, productSuffix);
            log.info("Fetching interest rate with code: {} for product: {}", rateCode, productCode);
            
            // Fetch interest rate from API
            com.btlab.fdcalculator.model.dto.ProductInterestDTO interestRate = 
                pricingApiClient.getInterestRateByCode(productCode, rateCode);
            
            if (interestRate == null) {
                log.warn("No interest rate found for code: {}. Using fallback rate.", rateCode);
                return rateCacheService.getBaseRate(productCode); // Fallback to cache
            }
            
            // Select rate based on cumulative flag
            BigDecimal rate;
            if (cumulative != null && cumulative) {
                rate = interestRate.rateCumulative();
                log.info("Using cumulative rate: {}%", rate);
            } else {
                // For non-cumulative, use payout frequency or compounding frequency
                String frequency = payoutFreq != null ? payoutFreq : compoundingFreq;
                if (frequency == null) frequency = "YEARLY";
                
                rate = switch (frequency.toUpperCase()) {
                    case "MONTHLY" -> interestRate.rateNonCumulativeMonthly();
                    case "QUARTERLY" -> interestRate.rateNonCumulativeQuarterly();
                    case "YEARLY" -> interestRate.rateNonCumulativeYearly();
                    default -> interestRate.rateNonCumulativeYearly();
                };
                log.info("Using non-cumulative {} rate: {}%", frequency, rate);
            }
            
            return rate != null ? rate : rateCacheService.getBaseRate(productCode);
            
        } catch (Exception e) {
            log.error("Error fetching interest rate from API: {}. Using fallback.", e.getMessage());
            return rateCacheService.getBaseRate(productCode); // Fallback to cache
        }
    }
}
