package com.btlab.fdcalculator.service.impl;

import com.btlab.fdcalculator.client.MockPricingApiClient;
import com.btlab.fdcalculator.model.dto.CategoryDTO;
import com.btlab.fdcalculator.service.CategoryService;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Profile("mock")
public class MockCategoryService implements CategoryService {

    private final MockPricingApiClient client = new MockPricingApiClient();

    @Override
    public List<CategoryDTO> getCategories() {
        return client.getCategories();
    }
}
