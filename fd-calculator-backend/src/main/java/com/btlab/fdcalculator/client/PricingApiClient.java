package com.btlab.fdcalculator.client;

import com.btlab.fdcalculator.model.dto.PagedProductRuleResponse;
import com.btlab.fdcalculator.model.dto.ProductDetailsDTO;
import com.btlab.fdcalculator.model.dto.ProductInterestDTO;
import com.btlab.fdcalculator.model.dto.ProductRuleDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "pricing-api", url = "${pricing.api.url}")
public interface PricingApiClient {

    // Get product details including interestType and compoundingFrequency
    @GetMapping("/api/products/{productCode}")
    ProductDetailsDTO getProductDetails(@PathVariable("productCode") String productCode);

    // UPDATED: This now calls the correct interest rates endpoint
    @GetMapping("/api/products/{productCode}/interest-rates")
    List<ProductInterestDTO> getInterestRates(@PathVariable("productCode") String productCode);

    // Get a specific interest rate by rateCode
    @GetMapping("/api/products/{productCode}/interest-rates/{rateCode}")
    ProductInterestDTO getInterestRateByCode(
        @PathVariable("productCode") String productCode,
        @PathVariable("rateCode") String rateCode
    );

    // Get all rules for a product (paginated response)
    @GetMapping("/api/products/{productCode}/rules")
    PagedProductRuleResponse getRules(
        @PathVariable("productCode") String productCode,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "100") int size
    );

    // Get a specific rule by ruleCode
    @GetMapping("/api/products/{productCode}/rules/{ruleCode}")
    ProductRuleDTO getRuleByCode(
        @PathVariable("productCode") String productCode,
        @PathVariable("ruleCode") String ruleCode
    );
}