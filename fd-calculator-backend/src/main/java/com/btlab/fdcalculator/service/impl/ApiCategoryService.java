package com.btlab.fdcalculator.service.impl;

import com.btlab.fdcalculator.client.PricingApiClient;
import com.btlab.fdcalculator.model.dto.CategoryDTO;
import com.btlab.fdcalculator.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Profile("!mock") // <-- This is the crucial line
@RequiredArgsConstructor
public class ApiCategoryService implements CategoryService {

    private final PricingApiClient pricingApiClient;

    @Override
    public List<CategoryDTO> getCategories() {
        return pricingApiClient.getCategories();
    }
}
