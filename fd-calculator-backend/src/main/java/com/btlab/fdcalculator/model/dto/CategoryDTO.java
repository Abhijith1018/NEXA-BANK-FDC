package com.btlab.fdcalculator.model.dto;

import java.math.BigDecimal;

public record CategoryDTO(Long category_id, String category_name, BigDecimal additional_percentage) {}
