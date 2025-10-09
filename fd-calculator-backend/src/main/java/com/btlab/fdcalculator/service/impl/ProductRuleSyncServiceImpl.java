package com.btlab.fdcalculator.service.impl;

import com.btlab.fdcalculator.client.PricingApiClient;
import com.btlab.fdcalculator.model.dto.CategoryDTO;
import com.btlab.fdcalculator.model.dto.PagedProductRuleResponse;
import com.btlab.fdcalculator.model.dto.ProductRuleDTO;
import com.btlab.fdcalculator.model.entity.Category;
import com.btlab.fdcalculator.repository.CategoryRepository;
import com.btlab.fdcalculator.service.ProductRuleSyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductRuleSyncServiceImpl implements ProductRuleSyncService {

    private final PricingApiClient pricingApiClient;
    private final CategoryRepository categoryRepository;

    // Mapping of rule code patterns to category names
    private static final Map<String, String> RULE_CODE_TO_CATEGORY_NAME = Map.ofEntries(
        Map.entry("MIN", "Minimum Amount"),
        Map.entry("MAX", "Maximum Amount"),
        Map.entry("MAXINT", "Maximum Excess Interest"),
        Map.entry("JR", "Junior Benefit (Under 18)"),
        Map.entry("SR", "Senior Citizen Benefit"),
        Map.entry("DY", "Digi Youth Benefit"),
        Map.entry("GOLD", "Gold Members Benefit"),
        Map.entry("SIL", "Silver Members Benefit"),
        Map.entry("PLAT", "Platinum Members Benefit"),
        Map.entry("EMP", "Employee Benefit")
    );

    @Override
    @Transactional
    public void syncProductRulesToCategories(String productCode) {
        log.info("Starting sync of product rules for product code: {}", productCode);
        
        try {
            // Extract the last 3 digits from product code (e.g., "FD001" -> "001")
            String productSuffix = extractProductSuffix(productCode);
            
            // Fetch all rules from the Product & Pricing API
            PagedProductRuleResponse response = pricingApiClient.getRules(productCode, 0, 100);
            List<ProductRuleDTO> rules = response.content();
            
            log.info("Fetched {} rules from Product & Pricing API", rules.size());
            
            // Process each rule and save/update in Category repository
            for (ProductRuleDTO rule : rules) {
                processAndSaveRule(rule, productSuffix);
            }
            
            log.info("Successfully synced {} rules to categories", rules.size());
            
        } catch (Exception e) {
            log.error("Error syncing product rules for product code: {}", productCode, e);
            throw new RuntimeException("Failed to sync product rules: " + e.getMessage(), e);
        }
    }

    private void processAndSaveRule(ProductRuleDTO rule, String productSuffix) {
        String ruleCode = rule.ruleCode();
        String ruleName = rule.ruleName();
        
        log.debug("Processing rule: {} - {}", ruleCode, ruleName);
        
        // Determine the category type from the rule code
        String categoryType = extractCategoryType(ruleCode, productSuffix);
        
        if (categoryType == null) {
            log.warn("Could not determine category type for rule code: {}", ruleCode);
            return;
        }
        
        // For rules like MIN, MAX, MAXINT - these are not categories but constraints
        // For rules like JR, SR, DY - these are benefit categories
        if (isBenefitCategory(categoryType)) {
            saveBenefitCategory(rule, categoryType);
        } else {
            log.debug("Rule {} is a constraint, not a benefit category. Skipping category creation.", ruleCode);
        }
    }

    private String extractCategoryType(String ruleCode, String productSuffix) {
        // Remove the product suffix from the rule code to get the category type
        // e.g., "MAX001" -> "MAX", "JR001" -> "JR", "MAXINT001" -> "MAXINT"
        
        // Check for MAXINT first (longer prefix)
        if (ruleCode.startsWith("MAXINT")) {
            return "MAXINT";
        }
        
        // Then check other prefixes
        for (String prefix : RULE_CODE_TO_CATEGORY_NAME.keySet()) {
            if (ruleCode.startsWith(prefix)) {
                return prefix;
            }
        }
        
        return null;
    }

    private boolean isBenefitCategory(String categoryType) {
        // Benefit categories are those that add extra interest
        // Constraint categories (MIN, MAX, MAXINT) are excluded
        return categoryType.equals("JR") || 
               categoryType.equals("SR") || 
               categoryType.equals("DY") ||
               categoryType.equals("GOLD") ||
               categoryType.equals("SIL") ||
               categoryType.equals("PLAT") ||
               categoryType.equals("EMP");
    }

    private void saveBenefitCategory(ProductRuleDTO rule, String categoryType) {
        String categoryName = RULE_CODE_TO_CATEGORY_NAME.get(categoryType);
        
        if (categoryName == null) {
            categoryName = rule.ruleName(); // Fallback to rule name
        }
        
        BigDecimal additionalPercentage;
        try {
            additionalPercentage = new BigDecimal(rule.ruleValue());
        } catch (NumberFormatException e) {
            log.error("Invalid rule value for rule {}: {}", rule.ruleCode(), rule.ruleValue());
            return;
        }
        
        // Check if category already exists
        Optional<Category> existingCategory = categoryRepository.findByCategoryName(categoryName);
        
        if (existingCategory.isPresent()) {
            // Update existing category
            Category category = existingCategory.get();
            category.setAdditionalPercentage(additionalPercentage);
            categoryRepository.save(category);
            log.info("Updated category: {} with value: {}", categoryName, additionalPercentage);
        } else {
            // Create new category
            Category newCategory = Category.builder()
                .categoryName(categoryName)
                .additionalPercentage(additionalPercentage)
                .build();
            categoryRepository.save(newCategory);
            log.info("Created new category: {} with value: {}", categoryName, additionalPercentage);
        }
    }

    private String extractProductSuffix(String productCode) {
        // Extract last 3 characters (e.g., "FD001" -> "001")
        if (productCode.length() >= 3) {
            return productCode.substring(productCode.length() - 3);
        }
        return productCode;
    }

    @Override
    public List<CategoryDTO> getAllCategories() {
        return categoryRepository.findAll().stream()
            .map(cat -> new CategoryDTO(
                cat.getCategoryId(),
                cat.getCategoryName(),
                cat.getAdditionalPercentage()
            ))
            .collect(Collectors.toList());
    }
}
