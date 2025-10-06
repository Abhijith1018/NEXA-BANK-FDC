package com.btlab.fdcalculator.service;

import com.btlab.fdcalculator.model.dto.CategoryDTO;
import org.springframework.stereotype.Service;

import java.util.List;

public interface CategoryService {
    List<CategoryDTO> getCategories();
}
