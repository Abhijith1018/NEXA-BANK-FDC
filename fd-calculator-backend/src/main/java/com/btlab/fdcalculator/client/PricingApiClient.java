package com.btlab.fdcalculator.client;

import com.btlab.fdcalculator.model.dto.CategoryDTO;
import com.btlab.fdcalculator.model.dto.ProductInterestDTO;
import com.btlab.fdcalculator.model.dto.ProductRuleDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(name = "pricing-api", url = "${pricing.api.url}")
public interface PricingApiClient {

    // UPDATED: This now calls the correct interest rates endpoint
    @GetMapping("/api/products/{productCode}/interest-rates")
    List<ProductInterestDTO> getInterestRates(@PathVariable("productCode") String productCode);

    // UPDATED: This now calls the correct rules endpoint (for categories)
    @GetMapping("/api/products/{productCode}/rules")
    List<ProductRuleDTO> getRules(@PathVariable("productCode") String productCode);
}