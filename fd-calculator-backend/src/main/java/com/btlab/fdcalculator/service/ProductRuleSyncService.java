package com.btlab.fdcalculator.service;

import java.util.List;

public interface ProductRuleSyncService {
    /**
     * Sync product rules from the Product & Pricing API to the Category repository
     * @param productCode The product code (e.g., "FD001")
     */
    void syncProductRulesToCategories(String productCode);
    
    /**
     * Get all synced categories from the database
     */
    List<com.btlab.fdcalculator.model.dto.CategoryDTO> getAllCategories();
}
