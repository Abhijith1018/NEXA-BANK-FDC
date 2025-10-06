package com.btlab.fdcalculator.client;

import com.btlab.fdcalculator.model.dto.CategoryDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.util.List;

@FeignClient(name = "pricing-api", url = "${pricing.api.url}")
public interface PricingApiClient {
    @GetMapping("/base-rate")
    BigDecimal getBaseRate(@RequestParam("productCode") String productCode);

    @GetMapping("/categories")
    List<CategoryDTO> getCategories();
}