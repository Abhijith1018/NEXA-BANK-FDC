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
        // 1. Call the new getRules() method from the client
        List<ProductRuleDTO> rules = client.getRules("mock-product");

        // 2. Transform the list of rules into the CategoryDTO format
        return rules.stream()
                .map(rule -> new CategoryDTO(
                        Long.parseLong(rule.ruleId()),
                        rule.ruleName(),
                        new BigDecimal(rule.ruleValue())
                ))
                .toList();
    }
}