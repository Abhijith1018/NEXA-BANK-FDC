package com.btlab.fdcalculator.service.impl;

import com.btlab.fdcalculator.client.PricingApiClient;
import com.btlab.fdcalculator.model.dto.CategoryDTO;
import com.btlab.fdcalculator.model.dto.ProductRuleDTO;
import com.btlab.fdcalculator.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@Profile("!mock") // <-- This is the crucial line
@RequiredArgsConstructor
public class ApiCategoryService implements CategoryService {

    private final PricingApiClient pricingApiClient;

    @Override
    public List<CategoryDTO> getCategories() {
        // The new endpoint requires a productCode. For now, we can hardcode a
        // default one, as categories are likely to be similar across products.
        String defaultProductCode = "FD001";

        // 1. Call the new Feign client method to get a paginated list of rules
        var pagedResponse = pricingApiClient.getRules(defaultProductCode, 0, 100);
        List<ProductRuleDTO> rules = pagedResponse.content();

        // 2. Filter only benefit categories (JR, SR, DY)
        // 3. Transform (map) the list of rules into the CategoryDTO format
        return rules.stream()
                .filter(rule -> isBenefitCategory(rule.ruleCode()))
                .map(rule -> new CategoryDTO(
                        Long.parseLong(rule.ruleId()), // Convert the String ID to a Long
                        rule.ruleName(),
                        new BigDecimal(rule.ruleValue()) // Convert the String rate value to BigDecimal
                ))
                .toList();
    }
    
    private boolean isBenefitCategory(String ruleCode) {
        // Only return benefit categories, not constraint rules
        return ruleCode.startsWith("JR") || 
               ruleCode.startsWith("SR") || 
               ruleCode.startsWith("DY");
    }
}
