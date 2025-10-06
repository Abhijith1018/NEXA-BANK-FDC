package com.btlab.fdcalculator.client;

import com.btlab.fdcalculator.model.dto.CategoryDTO;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
@Profile("mock")
public class MockPricingApiClient implements PricingApiClient {
    @Override
    public BigDecimal getBaseRate(String productCode) {
        return new BigDecimal("6.50");
    }

    @Override
    public List<CategoryDTO> getCategories() {
        return List.of(
            new CategoryDTO(1L, "Senior Citizen", new BigDecimal("0.50")),
            new CategoryDTO(2L, "Staff", new BigDecimal("1.00"))
        );
    }
}
