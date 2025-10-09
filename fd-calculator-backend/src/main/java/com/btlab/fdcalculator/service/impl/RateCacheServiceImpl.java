package com.btlab.fdcalculator.service.impl;

import com.btlab.fdcalculator.client.PricingApiClient;
import com.btlab.fdcalculator.model.dto.ProductInterestDTO;
import com.btlab.fdcalculator.model.entity.RateCache;
import com.btlab.fdcalculator.repository.RateCacheRepository;
import com.btlab.fdcalculator.service.RateCacheService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RateCacheServiceImpl implements RateCacheService {

    private final RateCacheRepository rateCacheRepository;
    private final PricingApiClient pricingApiClient;

    private static final long TTL_HOURS = 24;

    @Override
    public BigDecimal getBaseRate(String productCode) {
        return rateCacheRepository.findById(productCode)
                .filter(c -> !c.isStale(TTL_HOURS))
                .map(RateCache::getBaseRate)
                .orElseGet(() -> {
                    // 1. Call the new Feign client method to get all rate slabs
                    List<ProductInterestDTO> rates = pricingApiClient.getInterestRates(productCode);

                    // 2. Decide which rate to use. We will assume the cumulative rate
                    //    from the first available slab is the base rate.
                    //    This logic can be made more complex later if needed.
                    BigDecimal freshRate = rates.stream()
                            .findFirst()
                            .map(ProductInterestDTO::rateCumulative)
                            .orElse(BigDecimal.ZERO); // Default to 0 if no rates are found

                    // 3. Save the chosen rate to the cache
                    rateCacheRepository.save(RateCache.builder()
                            .productCode(productCode)
                            .baseRate(freshRate)
                            .lastUpdated(LocalDateTime.now())
                            .build());
                    return freshRate;
                });
    }

    @Override
    public void refreshRate(String productCode) {
        // 1. Call the API client to get the list of all interest rate slabs
        List<ProductInterestDTO> rates = pricingApiClient.getInterestRates(productCode);

        // 2. Extract the specific rate you want to cache. We'll use the same logic
        //    as getBaseRate: take the cumulative rate from the first available slab.
        BigDecimal freshRate = rates.stream()
                .findFirst()
                .map(ProductInterestDTO::rateCumulative)
                .orElse(BigDecimal.ZERO); // Default to 0 if no rates are found

        // 3. Save the single extracted BigDecimal rate to the cache
        rateCacheRepository.save(RateCache.builder()
                .productCode(productCode)
                .baseRate(freshRate)
                .lastUpdated(LocalDateTime.now())
                .build());
    }
}
