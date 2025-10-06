package com.btlab.fdcalculator.repository;

import com.btlab.fdcalculator.model.entity.FDCalculationResult;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FDCalculationResultRepository extends JpaRepository<FDCalculationResult, Long> {
    FDCalculationResult findByCalc_CalcId(Long calcId);
}
