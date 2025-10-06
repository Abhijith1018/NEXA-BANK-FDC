package com.btlab.fdcalculator.config;

import com.btlab.fdcalculator.model.entity.Category;
import com.btlab.fdcalculator.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@Profile("mock") // Ensures this only runs for the mock profile
@RequiredArgsConstructor
public class MockDataSeeder implements CommandLineRunner {

    private final CategoryRepository categoryRepository;

    @Override
    public void run(String... args) throws Exception {
        // Create and save a "General" category
        Category general = Category.builder()
                .categoryId(1L)
                .categoryName("General")
                .additionalPercentage(new BigDecimal("0.0"))
                .build();
        categoryRepository.save(general);

        // Create and save a "Senior Citizen" category
        Category senior = Category.builder()
                .categoryId(2L)
                .categoryName("Senior Citizen")
                .additionalPercentage(new BigDecimal("0.5"))
                .build();
        categoryRepository.save(senior);

        System.out.println("---- Mock database seeded with 2 categories. ----");
    }
}