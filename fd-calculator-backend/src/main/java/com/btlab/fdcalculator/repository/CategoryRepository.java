package com.btlab.fdcalculator.repository;

import com.btlab.fdcalculator.model.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {}
