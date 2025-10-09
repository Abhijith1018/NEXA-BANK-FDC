# Interest Rate Integration - Complete Guide

## Overview
This document describes the integration of dynamic interest rate fetching from the Product & Pricing API based on tenure and cumulative/non-cumulative selection.

## Changes Made

### 1. Request DTO Updates (`FDCalculationRequest.java`)

#### New Fields Added:
```java
Boolean cumulative,   // true for cumulative, false for non-cumulative
String payout_freq    // MONTHLY, QUARTERLY, or YEARLY (only for non-cumulative)
```

#### Sample Request - Cumulative:
```json
{
  "principal_amount": 50000.00,
  "tenure_value": 5,
  "tenure_unit": "YEARS",
  "interest_type": "COMPOUND",
  "compounding_frequency": "QUARTERLY",
  "currency_code": "INR",
  "category1_id": "SENIOR",
  "category2_id": "GOLD",
  "cumulative": true,
  "payout_freq": null,
  "product_code": "FD001"
}
```

#### Sample Request - Non-Cumulative:
```json
{
  "principal_amount": 50000.00,
  "tenure_value": 5,
  "tenure_unit": "YEARS",
  "interest_type": "COMPOUND",
  "compounding_frequency": "QUARTERLY",
  "currency_code": "INR",
  "category1_id": "SENIOR",
  "category2_id": "GOLD",
  "cumulative": false,
  "payout_freq": "MONTHLY",
  "product_code": "FD001"
}
```

### 2. Interest Rate DTO Updates (`ProductInterestDTO.java`)

#### Added Field:
```java
String rateCode  // e.g., "INT12M001", "INT24M001", "INT36M001", "INT60M001"
```

#### Complete Structure:
```java
public record ProductInterestDTO(
    String rateId,
    String rateCode,
    int termInMonths,
    BigDecimal rateCumulative,
    BigDecimal rateNonCumulativeMonthly,
    BigDecimal rateNonCumulativeQuarterly,
    BigDecimal rateNonCumulativeYearly
) {}
```

### 3. API Client Updates (`PricingApiClient.java`)

#### New Method Added:
```java
@GetMapping("/api/products/{productCode}/interest-rates/{rateCode}")
ProductInterestDTO getInterestRateByCode(
    @PathVariable("productCode") String productCode,
    @PathVariable("rateCode") String rateCode
);
```

### 4. Calculator Service Updates (`FDCalculatorServiceImpl.java`)

#### New Helper Methods:

##### A. Calculate Tenure in Months
```java
private int calculateTenureInMonths(int tenureValue, String tenureUnit) {
    return switch (tenureUnit.toUpperCase()) {
        case "DAYS" -> (int) Math.ceil(tenureValue / 30.0);
        case "MONTHS" -> tenureValue;
        case "YEARS" -> tenureValue * 12;
        default -> throw new IllegalArgumentException("Invalid tenure_unit");
    };
}
```

##### B. Construct Rate Code Based on Tenure
```java
private String constructRateCode(int tenureInMonths, String productSuffix) {
    String tenurePart;
    if (tenureInMonths <= 12) {
        tenurePart = "12M";
    } else if (tenureInMonths <= 24) {
        tenurePart = "24M";
    } else if (tenureInMonths <= 36) {
        tenurePart = "36M";
    } else {
        tenurePart = "60M";
    }
    return "INT" + tenurePart + productSuffix;
}
```

##### C. Get Base Rate from API
```java
private BigDecimal getBaseRateFromApi(String productCode, String productSuffix, 
                                      int tenureInMonths, Boolean cumulative, 
                                      String payoutFreq, String compoundingFreq) {
    // Construct rate code
    String rateCode = constructRateCode(tenureInMonths, productSuffix);
    
    // Fetch from API
    ProductInterestDTO interestRate = 
        pricingApiClient.getInterestRateByCode(productCode, rateCode);
    
    // Select rate based on cumulative flag
    if (cumulative != null && cumulative) {
        return interestRate.rateCumulative();
    } else {
        String frequency = payoutFreq != null ? payoutFreq : compoundingFreq;
        return switch (frequency.toUpperCase()) {
            case "MONTHLY" -> interestRate.rateNonCumulativeMonthly();
            case "QUARTERLY" -> interestRate.rateNonCumulativeQuarterly();
            case "YEARLY" -> interestRate.rateNonCumulativeYearly();
            default -> interestRate.rateNonCumulativeYearly();
        };
    }
}
```

### 5. Mock Client Updates (`MockPricingApiClient.java`)

#### Updated to Include All 4 Interest Rate Slabs:
```java
@Override
public ProductInterestDTO getInterestRateByCode(String productCode, String rateCode) {
    return switch (rateCode) {
        case "INT12M001" -> new ProductInterestDTO("rate1", "INT12M001", 12, 
            new BigDecimal("7.6"), new BigDecimal("7.4"), 
            new BigDecimal("7.5"), new BigDecimal("7.6"));
        case "INT24M001" -> new ProductInterestDTO("rate2", "INT24M001", 24, 
            new BigDecimal("7.7"), new BigDecimal("7.5"), 
            new BigDecimal("7.6"), new BigDecimal("7.7"));
        case "INT36M001" -> new ProductInterestDTO("rate3", "INT36M001", 36, 
            new BigDecimal("8.0"), new BigDecimal("7.85"), 
            new BigDecimal("7.9"), new BigDecimal("7.8"));
        case "INT60M001" -> new ProductInterestDTO("rate4", "INT60M001", 60, 
            new BigDecimal("8.5"), new BigDecimal("8.3"), 
            new BigDecimal("8.4"), new BigDecimal("8.5"));
        default -> throw new IllegalArgumentException("Interest rate not found");
    };
}
```

## Interest Rate Code Mapping

| Tenure Range | Rate Code | Description |
|--------------|-----------|-------------|
| 0-12 months | INT12M001 | 12-month slab rates |
| 12-24 months | INT24M001 | 24-month slab rates |
| 24-36 months | INT36M001 | 36-month slab rates |
| 36-60+ months | INT60M001 | 60-month slab rates (for 36+ months) |

**Format:** `INT{months}M{productSuffix}`
- Example: For FD001 product (suffix = 001) with 18-month tenure → `INT24M001`

## Rate Selection Logic

### For Cumulative FDs:
```
cumulative: true
→ Uses: rateCumulative
```

### For Non-Cumulative FDs:
```
cumulative: false
payout_freq: MONTHLY   → Uses: rateNonCumulativeMonthly
payout_freq: QUARTERLY → Uses: rateNonCumulativeQuarterly
payout_freq: YEARLY    → Uses: rateNonCumulativeYearly
```

If `payout_freq` is not provided, falls back to `compounding_frequency`.

## API Endpoints Used

### 1. Get All Interest Rates
```
GET /api/products/{productCode}/interest-rates
```

**Response:**
```json
{
  "content": [
    {
      "rateId": "e75bed67-db57-405d-9f35-0af9d0d62e70",
      "rateCode": "INT12M001",
      "termInMonths": 12,
      "rateCumulative": 7.6,
      "rateNonCumulativeMonthly": 7.4,
      "rateNonCumulativeQuarterly": 7.5,
      "rateNonCumulativeYearly": 7.6
    },
    ...
  ],
  ...
}
```

### 2. Get Specific Interest Rate
```
GET /api/products/{productCode}/interest-rates/{rateCode}
```

**Example:**
```
GET /api/products/FD001/interest-rates/INT12M001
```

**Response:**
```json
{
  "rateId": "e75bed67-db57-405d-9f35-0af9d0d62e70",
  "rateCode": "INT12M001",
  "termInMonths": 12,
  "rateCumulative": 7.6,
  "rateNonCumulativeMonthly": 7.4,
  "rateNonCumulativeQuarterly": 7.5,
  "rateNonCumulativeYearly": 7.6
}
```

## Calculation Flow

1. **Request Received**
   - Parse `tenure_value`, `tenure_unit`, `cumulative`, `payout_freq`

2. **Convert Tenure to Months**
   - DAYS: `tenure / 30` (rounded up)
   - MONTHS: `tenure`
   - YEARS: `tenure * 12`

3. **Determine Rate Code**
   - 0-12 months → INT12M001
   - 12-24 months → INT24M001
   - 24-36 months → INT36M001
   - 36+ months → INT60M001

4. **Fetch Interest Rate from API**
   - Call `/api/products/{productCode}/interest-rates/{rateCode}`

5. **Select Appropriate Rate**
   - If `cumulative = true`: Use `rateCumulative`
   - If `cumulative = false`: Use rate based on `payout_freq`

6. **Apply Category Benefits**
   - Fetch category benefits (SR001, GOLD001, etc.)
   - Add to base rate

7. **Cap at Maximum Excess Interest**
   - Check MAXINT001 rule
   - Cap total extra at maximum allowed (e.g., 2%)

8. **Calculate Maturity**
   - Apply effective rate to principal
   - Calculate maturity value

## Example Calculation

### Request:
```json
{
  "principal_amount": 100000,
  "tenure_value": 5,
  "tenure_unit": "YEARS",
  "interest_type": "COMPOUND",
  "compounding_frequency": "QUARTERLY",
  "cumulative": true,
  "category1_id": "SENIOR",
  "category2_id": "GOLD",
  "product_code": "FD001"
}
```

### Calculation Steps:
1. **Tenure in months**: 5 years = 60 months
2. **Rate code**: 60 months → `INT60M001`
3. **Base rate** (cumulative): 8.5%
4. **Category benefits**:
   - SENIOR (SR001): 0.75%
   - GOLD (GOLD001): 1.0%
   - Total extra: 1.75%
5. **Check MAXINT001**: 2% (OK, 1.75% < 2%)
6. **Effective rate**: 8.5% + 1.75% = 10.25%
7. **Calculate maturity** with 10.25% for 5 years quarterly compounding

## Fallback Mechanism

If API call fails or rate not found:
- Falls back to `RateCacheService.getBaseRate(productCode)`
- Logs warning message
- Continues calculation with cached rate

## Benefits

✅ **Dynamic Rate Management** - Rates managed centrally in Product & Pricing service  
✅ **Tenure-Based Rates** - Different rates for different tenure slabs  
✅ **Cumulative vs Non-Cumulative** - Separate rates for different payout options  
✅ **Real-Time Updates** - No need to sync rates to FD Calculator database  
✅ **Graceful Degradation** - Falls back to cache if API unavailable  

## Testing

### Test with Cumulative:
```bash
curl -X POST http://localhost:8081/api/fd-calculator/calculate \
  -H "Content-Type: application/json" \
  -d '{
    "principal_amount": 50000,
    "tenure_value": 3,
    "tenure_unit": "YEARS",
    "interest_type": "COMPOUND",
    "compounding_frequency": "QUARTERLY",
    "cumulative": true,
    "category1_id": "SENIOR",
    "category2_id": "GOLD",
    "product_code": "FD001"
  }'
```

### Test with Non-Cumulative:
```bash
curl -X POST http://localhost:8081/api/fd-calculator/calculate \
  -H "Content-Type: application/json" \
  -d '{
    "principal_amount": 50000,
    "tenure_value": 2,
    "tenure_unit": "YEARS",
    "interest_type": "COMPOUND",
    "compounding_frequency": "QUARTERLY",
    "cumulative": false,
    "payout_freq": "MONTHLY",
    "category1_id": "SENIOR",
    "product_code": "FD001"
  }'
```

---

## Summary

The FD Calculator now fetches interest rates dynamically from the Product & Pricing API based on:
1. **Tenure** (converted to months and mapped to rate slabs)
2. **Cumulative flag** (cumulative vs non-cumulative)
3. **Payout frequency** (monthly/quarterly/yearly for non-cumulative)

This provides maximum flexibility and centralized rate management while maintaining backward compatibility through the fallback mechanism.
