# FD Calculator - Product Rules Integration Summary

## What Was Implemented

### 1. **Product Rules API Integration**

Created integration with the Product & Pricing API to dynamically fetch and use product rules instead of hardcoded values.

**Files Created/Modified:**
- `PricingApiClient.java` - Added methods to fetch rules (paginated and by code)
- `PagedProductRuleResponse.java` - DTO for paginated API responses
- `CategoryRepository.java` - Added `findByCategoryName()` method

### 2. **Rule Naming Convention**

The system follows this naming convention based on product codes:

For `FD001` (last 3 digits = `001`):
- `MIN001` → Minimum deposit amount
- `MAX001` → Maximum deposit amount  
- `MAXINT001` → Maximum excess interest percentage
- `JR001` → Junior benefit (under 18)
- `SR001` → Senior citizen benefit
- `DY001` → Digi Youth benefit

### 3. **Product Rule Sync Service**

**Files:**
- `ProductRuleSyncService.java` - Interface
- `ProductRuleSyncServiceImpl.java` - Implementation

**What it does:**
- Fetches all rules from Product & Pricing API for a given product code
- Identifies benefit categories (JR, SR, DY) vs constraints (MIN, MAX, MAXINT)
- Saves benefit categories to the Category repository
- Maps rule codes to friendly category names
- Updates existing categories or creates new ones

### 4. **Product Rule Validation Service**

**Files:**
- `ProductRuleValidationService.java` - Interface
- `ProductRuleValidationServiceImpl.java` - Implementation

**What it does:**
- Validates deposit amounts against MIN/MAX rules from the API
- Retrieves dynamic minimum amounts (MIN + suffix)
- Retrieves dynamic maximum amounts (MAX + suffix)
- Retrieves dynamic maximum excess interest (MAXINT + suffix)
- Provides fallback defaults if API calls fail

### 5. **Admin Controller**

**File:** `AdminController.java`

**Endpoints:**
```
POST /api/admin/sync-product-rules/{productCode}
GET  /api/admin/categories
```

**Purpose:**
- Trigger manual sync of product rules to categories
- View all synced categories in the database

### 6. **Updated FD Calculator Service**

**Modified:** `FDCalculatorServiceImpl.java`

**Changes:**
1. Injected `ProductRuleValidationService`
2. Added amount validation before calculation
3. Changed default product code from "FD_STD" to "FD001"
4. Made maximum excess interest dynamic (fetched from MAXINT rule)

## How It Works

### Step 1: Sync Product Rules

```bash
POST http://localhost:8080/api/admin/sync-product-rules/FD001
```

This fetches rules from:
```
GET http://localhost:8080/api/products/FD001/rules
```

And creates/updates categories in the database:
- Junior Benefit (Under 18) → 0.5% additional
- Senior Citizen Benefit → 0.75% additional
- Digi Youth Benefit → 0.25% additional

### Step 2: Calculate FD

```bash
POST http://localhost:8081/api/fd-calculator/calculate
{
  "product_code": "FD001",
  "principal_amount": 50000,
  "tenure_value": 12,
  "tenure_unit": "MONTHS",
  "interest_type": "COMPOUND",
  "compounding_frequency": "QUARTERLY",
  "category1_id": 1,  // e.g., Junior Benefit
  "category2_id": 2   // e.g., Senior Citizen Benefit
}
```

The system:
1. **Validates amount**: Checks 50000 is between MIN001 (10000) and MAX001 (500000)
2. **Gets base rate**: Fetches base rate for FD001
3. **Applies benefits**: Adds 0.5% (Junior) + 0.75% (Senior) = 1.25%
4. **Caps excess**: Ensures total extra ≤ MAXINT001 (2%)
5. **Calculates**: Computes maturity value with effective rate

## Key Features

### ✅ Dynamic Constraints
- Min/Max amounts come from API, not hardcoded
- Maximum excess interest is configurable per product

### ✅ Centralized Management
- All product rules managed in Product & Pricing service
- FD Calculator syncs and uses them dynamically

### ✅ Rule Code Pattern Matching
- Automatic extraction of product suffix (last 3 digits)
- Pattern-based rule code construction (MIN + suffix, etc.)

### ✅ Benefit Categories
- Only benefit rules (JR, SR, DY) are saved as categories
- Constraint rules (MIN, MAX, MAXINT) are used for validation only

### ✅ Fallback Handling
- Default values used if API calls fail
- Graceful degradation ensures system still works

## Example Product Rules Response

```json
{
  "content": [
    {
      "ruleId": "37fe7f8a-d9ea-4661-8ac9-dc3dc3ea5dcd",
      "ruleCode": "MAX001",
      "ruleName": "Maximum for FD001",
      "ruleType": "SIMPLE",
      "dataType": "NUMBER",
      "ruleValue": "500000",
      "validationType": "MIN_MAX"
    },
    {
      "ruleId": "a9e3f3f3-a120-41a3-8e5e-b2c1f0b5e080",
      "ruleCode": "MIN001",
      "ruleName": "Minimum for FD001",
      "ruleType": "SIMPLE",
      "dataType": "NUMBER",
      "ruleValue": "10000",
      "validationType": "MIN_MAX"
    },
    {
      "ruleId": "363ffabc-0827-46d3-bcd1-adae3f42ffe3",
      "ruleCode": "MAXINT001",
      "ruleName": "Maximum excess interest",
      "ruleType": "SIMPLE",
      "dataType": "PERCENTAGE",
      "ruleValue": "2",
      "validationType": "MIN_MAX"
    },
    {
      "ruleId": "1baae6cf-106a-444d-b7a7-85b889f329d8",
      "ruleCode": "JR001",
      "ruleName": "Extra Interest for under 18",
      "ruleType": "SIMPLE",
      "dataType": "PERCENTAGE",
      "ruleValue": "0.5",
      "validationType": "EXACT"
    },
    {
      "ruleId": "c478c7af-9d64-41a6-b2e2-45552054c85d",
      "ruleCode": "SR001",
      "ruleName": "Extra Interest for sr",
      "ruleType": "SIMPLE",
      "dataType": "PERCENTAGE",
      "ruleValue": "0.75",
      "validationType": "EXACT"
    },
    {
      "ruleId": "fb409ac1-149f-4163-8e88-835da99bb8bc",
      "ruleCode": "DY001",
      "ruleName": "Extra Interest for Digi Youth",
      "ruleType": "SIMPLE",
      "dataType": "PERCENTAGE",
      "ruleValue": "0.25",
      "validationType": "EXACT"
    }
  ]
}
```

## Configuration

**application.yml:**
```yaml
pricing:
  api:
    url: http://localhost:8080
    
server:
  port: 8081
```

The FD Calculator runs on port 8081 and connects to Product & Pricing API on port 8080.

## Testing the Integration

### 1. Start Product & Pricing API
Ensure it's running on port 8080 with FD001 rules configured.

### 2. Start FD Calculator
Run the FD Calculator application on port 8081.

### 3. Sync Rules
```bash
curl -X POST http://localhost:8081/api/admin/sync-product-rules/FD001
```

Expected response:
```json
{
  "status": "success",
  "message": "Successfully synced product rules for FD001"
}
```

### 4. View Synced Categories
```bash
curl http://localhost:8081/api/admin/categories
```

### 5. Calculate FD
```bash
curl -X POST http://localhost:8081/api/fd-calculator/calculate \
  -H "Content-Type: application/json" \
  -d '{
    "product_code": "FD001",
    "currency_code": "INR",
    "principal_amount": 50000,
    "tenure_value": 12,
    "tenure_unit": "MONTHS",
    "interest_type": "COMPOUND",
    "compounding_frequency": "QUARTERLY",
    "category1_id": 1,
    "category2_id": 2
  }'
```

## Error Scenarios

### Amount Too Low
```json
{
  "principal_amount": 5000  // Below MIN001 (10000)
}
```
Error: "Amount 5000.00 is below minimum allowed amount 10000.00 for product FD001"

### Amount Too High
```json
{
  "principal_amount": 600000  // Above MAX001 (500000)
}
```
Error: "Amount 600000.00 exceeds maximum allowed amount 500000.00 for product FD001"

## Future Enhancements

1. **Caching**: Cache product rules to reduce API calls
2. **Scheduled Sync**: Automatically sync rules periodically
3. **Multiple Products**: Support syncing multiple products at once
4. **Rule Versioning**: Track changes to rules over time
5. **Audit Logging**: Log all rule fetches and validations

## Summary

The FD Calculator now seamlessly integrates with the Product & Pricing API to:
- ✅ Fetch product rules dynamically
- ✅ Validate amounts against product-specific constraints
- ✅ Apply category-based benefits
- ✅ Use configurable maximum excess interest
- ✅ Maintain data consistency between systems

All product rules are centrally managed in the Product & Pricing service, making the system more maintainable and flexible.
