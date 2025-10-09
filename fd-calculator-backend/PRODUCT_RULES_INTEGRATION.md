# Product Rules Integration

This document explains how the FD Calculator integrates with the Product & Pricing API to fetch and use product rules.

## Overview

The FD Calculator now fetches product rules dynamically from the Product & Pricing API instead of using hardcoded values. This allows for centralized management of:

- Minimum and maximum deposit amounts
- Maximum excess interest rates
- Category-based benefits (Junior, Senior Citizen, Digi Youth, etc.)

## Rule Naming Convention

Product rules follow a specific naming convention based on the product code:

For a product code like `FD001`, the last 3 digits (`001`) are used as a suffix for rule codes:

| Rule Code Pattern | Purpose | Example | Description |
|------------------|---------|---------|-------------|
| `MIN` + suffix | Minimum Amount | `MIN001` | Minimum deposit amount allowed |
| `MAX` + suffix | Maximum Amount | `MAX001` | Maximum deposit amount allowed |
| `MAXINT` + suffix | Max Excess Interest | `MAXINT001` | Maximum additional interest percentage |
| `JR` + suffix | Junior Benefit | `JR001` | Extra interest for customers under 18 |
| `SR` + suffix | Senior Citizen Benefit | `SR001` | Extra interest for senior citizens |
| `DY` + suffix | Digi Youth Benefit | `DY001` | Extra interest for digital youth accounts |

## API Endpoints

### Product & Pricing API

The FD Calculator integrates with the following Product & Pricing API endpoints:

1. **Get All Rules (Paginated)**
   ```
   GET /api/products/{productCode}/rules?page=0&size=100
   ```

2. **Get Specific Rule**
   ```
   GET /api/products/{productCode}/rules/{ruleCode}
   ```

Example:
```bash
# Get all rules for FD001
GET http://localhost:8080/api/products/FD001/rules

# Get specific rule
GET http://localhost:8080/api/products/FD001/rules/MAX001
```

### FD Calculator Admin Endpoints

New admin endpoints for managing product rules:

1. **Sync Product Rules to Categories**
   ```
   POST /api/admin/sync-product-rules/{productCode}
   ```
   
   This endpoint:
   - Fetches all rules from the Product & Pricing API for the given product code
   - Identifies benefit categories (JR, SR, DY)
   - Saves/updates them in the Category repository
   - Constraint rules (MIN, MAX, MAXINT) are used for validation but not stored as categories

   Example:
   ```bash
   POST http://localhost:8080/api/admin/sync-product-rules/FD001
   ```

2. **Get All Synced Categories**
   ```
   GET /api/admin/categories
   ```

## Components

### 1. DTOs

- `ProductRuleDTO` - Represents a product rule from the API
- `PagedProductRuleResponse` - Paginated response wrapper for rules

### 2. Services

#### ProductRuleSyncService
- Syncs product rules from the Product & Pricing API to the local Category repository
- Maps rule codes to category names
- Only syncs benefit categories (JR, SR, DY)

#### ProductRuleValidationService
- Validates deposit amounts against MIN/MAX rules
- Retrieves constraint values dynamically from the Product & Pricing API
- Provides maximum excess interest limits

### 3. Updated FDCalculatorService

The calculation service now:
1. Validates the principal amount against product-specific MIN/MAX rules
2. Uses dynamic maximum excess interest from MAXINT rules
3. Applies category benefits from synced rules

## Usage Flow

### 1. Initial Setup - Sync Product Rules

Before performing FD calculations, sync the product rules:

```bash
POST http://localhost:8080/api/admin/sync-product-rules/FD001
```

This will:
- Fetch rules from `http://localhost:8080/api/products/FD001/rules`
- Create/update categories for JR001, SR001, DY001
- Store them in the Category table with their respective benefit percentages

### 2. Perform FD Calculation

When calculating FD maturity:

```json
POST /api/fd-calculator/calculate
{
  "product_code": "FD001",
  "currency_code": "INR",
  "principal_amount": 50000,
  "tenure_value": 12,
  "tenure_unit": "MONTHS",
  "interest_type": "COMPOUND",
  "compounding_frequency": "QUARTERLY",
  "category1_id": 1,
  "category2_id": 2
}
```

The system will:
1. Validate `principal_amount` is between MIN001 and MAX001
2. Fetch base rate for FD001
3. Apply category benefits (e.g., JR001 + SR001)
4. Cap total excess interest at MAXINT001
5. Calculate maturity value

## Example Product Rules for FD001

```json
{
  "content": [
    {
      "ruleCode": "MIN001",
      "ruleName": "Minimum for FD001",
      "ruleValue": "10000",
      "dataType": "NUMBER"
    },
    {
      "ruleCode": "MAX001",
      "ruleName": "Maximum for FD001",
      "ruleValue": "500000",
      "dataType": "NUMBER"
    },
    {
      "ruleCode": "MAXINT001",
      "ruleName": "Maximum excess interest",
      "ruleValue": "2",
      "dataType": "PERCENTAGE"
    },
    {
      "ruleCode": "JR001",
      "ruleName": "Extra Interest for under 18",
      "ruleValue": "0.5",
      "dataType": "PERCENTAGE"
    },
    {
      "ruleCode": "SR001",
      "ruleName": "Extra Interest for sr",
      "ruleValue": "0.75",
      "dataType": "PERCENTAGE"
    },
    {
      "ruleCode": "DY001",
      "ruleName": "Extra Interest for Digi Youth",
      "ruleValue": "0.25",
      "dataType": "PERCENTAGE"
    }
  ]
}
```

## Configuration

Ensure the Product & Pricing API URL is configured in `application.yml`:

```yaml
pricing:
  api:
    url: http://localhost:8080
```

## Error Handling

- If product rules cannot be fetched, the system uses default fallback values
- Invalid amounts trigger validation exceptions with clear error messages
- Failed sync operations return error responses with details
