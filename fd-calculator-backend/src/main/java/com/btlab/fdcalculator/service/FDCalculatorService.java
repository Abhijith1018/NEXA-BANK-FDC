package com.btlab.fdcalculator.service;

import com.btlab.fdcalculator.model.dto.FDCalculationRequest;
import com.btlab.fdcalculator.model.dto.FDCalculationResponse;
import org.springframework.stereotype.Service;


public interface FDCalculatorService {
    FDCalculationResponse calculate(FDCalculationRequest request);
    FDCalculationResponse getByCalcId(Long calcId);
}
