package com.btlab.fdcalculator.controller;

import com.btlab.fdcalculator.model.dto.CategoryDTO;
import com.btlab.fdcalculator.model.entity.RateCache;
import com.btlab.fdcalculator.repository.RateCacheRepository;
import com.btlab.fdcalculator.service.CategoryService;
import com.btlab.fdcalculator.service.RateCacheService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/fd")
@RequiredArgsConstructor
public class ReferenceDataController {

    private final CategoryService categoryService;
    private final RateCacheService rateCacheService;
    private final RateCacheRepository rateCacheRepository;

    @GetMapping("/categories")
    public List<CategoryDTO> categories() {
        return categoryService.getCategories();
    }

    @GetMapping("/currencies")
    public List<String> currencies() {
        return List.of("INR","JPY","AED");
    }

    @GetMapping("/compounding-options")
    public List<String> compounding() {
        return List.of("DAILY","MONTHLY","QUARTERLY","YEARLY");
    }

    @PostMapping("/rate-cache/refresh")
    public String refresh(@RequestParam String productCode) {
        rateCacheService.refreshRate(productCode);
        return "Refreshed " + productCode;
    }

    @GetMapping("/rate-cache/{productCode}")
    public BigDecimal getRate(@PathVariable String productCode) {
        return rateCacheRepository.findById(productCode)
            .map(RateCache::getBaseRate)
            .orElse(null);
    }
}
