package com.btlab.fdcalculator.service;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;

public interface RateCacheService {
    BigDecimal getBaseRate(String productCode);
    void refreshRate(String productCode);
}
