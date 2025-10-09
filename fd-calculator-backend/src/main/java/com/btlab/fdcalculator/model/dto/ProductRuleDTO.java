package com.btlab.fdcalculator.model.dto;

public record ProductRuleDTO(
        String ruleId,
        String ruleCode,
        String ruleName,
        String ruleType,
        String dataType,
        String ruleValue,
        String validationType
) {}