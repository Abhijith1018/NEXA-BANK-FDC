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
@Profile("mock")
@RequiredArgsConstructor
public class MockCategoryService implements CategoryService {

    private final PricingApiClient client;

    @Override
    public List<CategoryDTO> getCategories() {
        // 1. Call the new getRules() method from the client with pagination
        var pagedResponse = client.getRules("mock-product", 0, 100);
        List<ProductRuleDTO> rules = pagedResponse.content();

        // 2. Filter only benefit categories (JR, SR, DY)
        // 3. Transform the list of rules into the CategoryDTO format
        return rules.stream()
                .filter(rule -> isBenefitCategory(rule.ruleCode()))
                .map(rule -> new CategoryDTO(
                        Long.parseLong(rule.ruleId()),
                        rule.ruleName(),
                        new BigDecimal(rule.ruleValue())
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