package com.btlab.fdcalculator.controller;

import com.btlab.fdcalculator.model.dto.FDCalculationRequest;
import com.btlab.fdcalculator.model.dto.FDCalculationResponse;
import com.btlab.fdcalculator.repository.FDCalculationInputRepository;
import com.btlab.fdcalculator.service.FDCalculatorService;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/fd")
@RequiredArgsConstructor
public class FDCalculatorController {

    private final FDCalculatorService fdCalculatorService;
    private final FDCalculationInputRepository inputRepo;

    @PostMapping("/calculate")
    public FDCalculationResponse calculate( @RequestBody FDCalculationRequest request) {
        return fdCalculatorService.calculate(request);
    }

    @GetMapping("/calculations/{calcId}")
    public FDCalculationResponse getOne(@PathVariable Long calcId) {
        return fdCalculatorService.getByCalcId(calcId);
    }

    @GetMapping("/history")
    public List<Long> history() {
        return inputRepo.findAll().stream().map(i -> i.getCalcId()).toList();
    }
}
