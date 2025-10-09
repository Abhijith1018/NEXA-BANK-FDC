package com.btlab.fdcalculator.service.impl;

import com.btlab.fdcalculator.client.PricingApiClient;
import com.btlab.fdcalculator.model.dto.ProductRuleDTO;
import com.btlab.fdcalculator.service.ProductRuleValidationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductRuleValidationServiceImpl implements ProductRuleValidationService {

    private final PricingApiClient pricingApiClient;

    @Override
    public void validateAmount(String productCode, BigDecimal amount) {
        BigDecimal minAmount = getMinimumAmount(productCode);
        BigDecimal maxAmount = getMaximumAmount(productCode);
        
        if (amount.compareTo(minAmount) < 0) {
            throw new IllegalArgumentException(
                String.format("Amount %.2f is below minimum allowed amount %.2f for product %s", 
                    amount, minAmount, productCode)
            );
        }
        
        if (amount.compareTo(maxAmount) > 0) {
            throw new IllegalArgumentException(
                String.format("Amount %.2f exceeds maximum allowed amount %.2f for product %s", 
                    amount, maxAmount, productCode)
            );
        }
    }

    @Override
    public BigDecimal getMinimumAmount(String productCode) {
        String suffix = extractProductSuffix(productCode);
        String ruleCode = "MIN" + suffix;
        
        try {
            ProductRuleDTO rule = pricingApiClient.getRuleByCode(productCode, ruleCode);
            return new BigDecimal(rule.ruleValue());
        } catch (Exception e) {
            log.warn("Could not fetch MIN rule for product {}. Using default minimum.", productCode);
            return BigDecimal.ZERO;
        }
    }

    @Override
    public BigDecimal getMaximumAmount(String productCode) {
        String suffix = extractProductSuffix(productCode);
        String ruleCode = "MAX" + suffix;
        
        try {
            ProductRuleDTO rule = pricingApiClient.getRuleByCode(productCode, ruleCode);
            return new BigDecimal(rule.ruleValue());
        } catch (Exception e) {
            log.warn("Could not fetch MAX rule for product {}. Using default maximum.", productCode);
            return new BigDecimal("999999999");
        }
    }

    @Override
    public BigDecimal getMaximumExcessInterest(String productCode) {
        String suffix = extractProductSuffix(productCode);
        String ruleCode = "MAXINT" + suffix;
        
        try {
            ProductRuleDTO rule = pricingApiClient.getRuleByCode(productCode, ruleCode);
            return new BigDecimal(rule.ruleValue());
        } catch (Exception e) {
            log.warn("Could not fetch MAXINT rule for product {}. Using default maximum.", productCode);
            return new BigDecimal("2.00");
        }
    }

    private String extractProductSuffix(String productCode) {
        // Extract last 3 characters (e.g., "FD001" -> "001")
        if (productCode != null && productCode.length() >= 3) {
            return productCode.substring(productCode.length() - 3);
        }
        return "001"; // Default suffix
    }
}
