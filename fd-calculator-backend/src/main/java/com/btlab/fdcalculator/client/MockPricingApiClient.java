package com.btlab.fdcalculator.client;

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
        // Return a mock list of interest rate slabs
        return List.of(
                new ProductInterestDTO("rate1", 12, new BigDecimal("6.50"), new BigDecimal("6.40"), new BigDecimal("6.45"), new BigDecimal("6.50"))
        );
    }

    @Override
    public List<ProductRuleDTO> getRules(String productCode) {
        // Return a mock list of rules that will act as categories
        return List.of(
                new ProductRuleDTO("1", "SENIOR_CITIZEN", "Senior Citizen", "SIMPLE", "PERCENTAGE", "0.50", "EXACT"),
                new ProductRuleDTO("2", "STAFF", "Staff", "SIMPLE", "PERCENTAGE", "1.00", "EXACT")
        );
    }
}