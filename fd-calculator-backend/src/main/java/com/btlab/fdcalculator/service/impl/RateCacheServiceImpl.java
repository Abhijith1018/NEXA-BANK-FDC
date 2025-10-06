package com.btlab.fdcalculator.service.impl;

import com.btlab.fdcalculator.client.PricingApiClient;
import com.btlab.fdcalculator.model.entity.RateCache;
import com.btlab.fdcalculator.repository.RateCacheRepository;
import com.btlab.fdcalculator.service.RateCacheService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

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
                BigDecimal fresh = pricingApiClient.getBaseRate(productCode);
                rateCacheRepository.save(RateCache.builder()
                    .productCode(productCode)
                    .baseRate(fresh)
                    .lastUpdated(LocalDateTime.now())
                    .build());
                return fresh;
            });
    }

    @Override
    public void refreshRate(String productCode) {
        BigDecimal fresh = pricingApiClient.getBaseRate(productCode);
        rateCacheRepository.save(RateCache.builder()
            .productCode(productCode)
            .baseRate(fresh)
            .lastUpdated(LocalDateTime.now())
            .build());
    }
}
