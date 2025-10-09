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

        // 1. Call the new Feign client method to get a list of rules
        List<ProductRuleDTO> rules = pricingApiClient.getRules(defaultProductCode);

        // 2. Transform (map) the list of rules into the CategoryDTO format
        return rules.stream()
                .map(rule -> new CategoryDTO(
                        Long.parseLong(rule.ruleId()), // Convert the String ID to a Long
                        rule.ruleName(),
                        new BigDecimal(rule.ruleValue()) // Convert the String rate value to BigDecimal
                ))
                .toList();
    }
}
