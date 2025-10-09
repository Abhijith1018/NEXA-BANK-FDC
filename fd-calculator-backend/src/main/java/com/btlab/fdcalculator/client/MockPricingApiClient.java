package com.btlab.fdcalculator.client;

import com.btlab.fdcalculator.model.dto.PagedProductRuleResponse;
import com.btlab.fdcalculator.model.dto.ProductInterestDTO;
import com.btlab.fdcalculator.model.dto.ProductRuleDTO;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
@Profile("mock")
public class MockPricingApiClient implements PricingApiClient {

    @Override
    public List<ProductInterestDTO> getInterestRates(String productCode) {
        // Return a mock list of interest rate slabs matching the API response
        return List.of(
                new ProductInterestDTO("rate1", "INT12M001", 12, 
                    new BigDecimal("7.6"), new BigDecimal("7.4"), 
                    new BigDecimal("7.5"), new BigDecimal("7.6")),
                new ProductInterestDTO("rate2", "INT24M001", 24, 
                    new BigDecimal("7.7"), new BigDecimal("7.5"), 
                    new BigDecimal("7.6"), new BigDecimal("7.7")),
                new ProductInterestDTO("rate3", "INT36M001", 36, 
                    new BigDecimal("8.0"), new BigDecimal("7.85"), 
                    new BigDecimal("7.9"), new BigDecimal("7.8")),
                new ProductInterestDTO("rate4", "INT60M001", 60, 
                    new BigDecimal("8.5"), new BigDecimal("8.3"), 
                    new BigDecimal("8.4"), new BigDecimal("8.5"))
        );
    }

    @Override
    public ProductInterestDTO getInterestRateByCode(String productCode, String rateCode) {
        // Return a specific interest rate based on rate code
        return switch (rateCode) {
            case "INT12M001" -> new ProductInterestDTO("rate1", "INT12M001", 12, 
                new BigDecimal("7.6"), new BigDecimal("7.4"), 
                new BigDecimal("7.5"), new BigDecimal("7.6"));
            case "INT24M001" -> new ProductInterestDTO("rate2", "INT24M001", 24, 
                new BigDecimal("7.7"), new BigDecimal("7.5"), 
                new BigDecimal("7.6"), new BigDecimal("7.7"));
            case "INT36M001" -> new ProductInterestDTO("rate3", "INT36M001", 36, 
                new BigDecimal("8.0"), new BigDecimal("7.85"), 
                new BigDecimal("7.9"), new BigDecimal("7.8"));
            case "INT60M001" -> new ProductInterestDTO("rate4", "INT60M001", 60, 
                new BigDecimal("8.5"), new BigDecimal("8.3"), 
                new BigDecimal("8.4"), new BigDecimal("8.5"));
            default -> throw new IllegalArgumentException("Interest rate not found: " + rateCode);
        };
    }

    @Override
    public PagedProductRuleResponse getRules(String productCode, int page, int size) {
        // Return a mock paginated list of rules
        List<ProductRuleDTO> rules = List.of(
                new ProductRuleDTO("1", "MIN001", "Minimum for FD001", "SIMPLE", "NUMBER", "10000", "MIN_MAX"),
                new ProductRuleDTO("2", "MAX001", "Maximum for FD001", "SIMPLE", "NUMBER", "500000", "MIN_MAX"),
                new ProductRuleDTO("3", "MAXINT001", "Maximum excess interest", "SIMPLE", "PERCENTAGE", "2", "MIN_MAX"),
                new ProductRuleDTO("4", "JR001", "Extra Interest for under 18", "SIMPLE", "PERCENTAGE", "0.5", "EXACT"),
                new ProductRuleDTO("5", "SR001", "Extra Interest for sr", "SIMPLE", "PERCENTAGE", "0.75", "EXACT"),
                new ProductRuleDTO("6", "DY001", "Extra Interest for Digi Youth", "SIMPLE", "PERCENTAGE", "0.25", "EXACT"),
                new ProductRuleDTO("7", "GOLD001", "Gold members extra interest", "SIMPLE", "NUMBER", "1", "EXACT"),
                new ProductRuleDTO("8", "SIL001", "Silver members extra interest", "SIMPLE", "NUMBER", "0.5", "EXACT"),
                new ProductRuleDTO("9", "PLAT001", "Platinum members extra interest", "SIMPLE", "NUMBER", "1.5", "EXACT"),
                new ProductRuleDTO("10", "EMP001", "Employee members extra interest", "SIMPLE", "NUMBER", "1.5", "EXACT")
        );
        
        PagedProductRuleResponse.SortInfo sortInfo = new PagedProductRuleResponse.SortInfo(true, true, false);
        PagedProductRuleResponse.PageableInfo pageableInfo = new PagedProductRuleResponse.PageableInfo(
                page, size, sortInfo, 0, false, true
        );
        
        return new PagedProductRuleResponse(
                rules,
                pageableInfo,
                true,  // last
                rules.size(),  // totalElements
                1,  // totalPages
                true,  // first
                size,  // size
                page,  // number
                sortInfo,
                rules.size(),  // numberOfElements
                false  // empty
        );
    }

    @Override
    public ProductRuleDTO getRuleByCode(String productCode, String ruleCode) {
        // Return a mock rule based on the rule code
        return switch (ruleCode) {
            case "MIN001" -> new ProductRuleDTO("1", "MIN001", "Minimum for FD001", "SIMPLE", "NUMBER", "10000", "MIN_MAX");
            case "MAX001" -> new ProductRuleDTO("2", "MAX001", "Maximum for FD001", "SIMPLE", "NUMBER", "500000", "MIN_MAX");
            case "MAXINT001" -> new ProductRuleDTO("3", "MAXINT001", "Maximum excess interest", "SIMPLE", "PERCENTAGE", "2", "MIN_MAX");
            case "JR001" -> new ProductRuleDTO("4", "JR001", "Extra Interest for under 18", "SIMPLE", "PERCENTAGE", "0.5", "EXACT");
            case "SR001" -> new ProductRuleDTO("5", "SR001", "Extra Interest for sr", "SIMPLE", "PERCENTAGE", "0.75", "EXACT");
            case "DY001" -> new ProductRuleDTO("6", "DY001", "Extra Interest for Digi Youth", "SIMPLE", "PERCENTAGE", "0.25", "EXACT");
            case "GOLD001" -> new ProductRuleDTO("7", "GOLD001", "Gold members extra interest", "SIMPLE", "NUMBER", "1", "EXACT");
            case "SIL001" -> new ProductRuleDTO("8", "SIL001", "Silver members extra interest", "SIMPLE", "NUMBER", "0.5", "EXACT");
            case "PLAT001" -> new ProductRuleDTO("9", "PLAT001", "Platinum members extra interest", "SIMPLE", "NUMBER", "1.5", "EXACT");
            case "EMP001" -> new ProductRuleDTO("10", "EMP001", "Employee members extra interest", "SIMPLE", "NUMBER", "1.5", "EXACT");
            default -> throw new IllegalArgumentException("Rule not found: " + ruleCode);
        };
    }
}