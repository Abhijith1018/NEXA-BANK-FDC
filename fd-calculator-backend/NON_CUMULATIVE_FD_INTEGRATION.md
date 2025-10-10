# Non-Cumulative FD Integration - Payout Frequency & Amount

## Overview
This document describes the implementation of non-cumulative FD support with periodic interest payouts. Non-cumulative FDs pay interest periodically (monthly, quarterly, or yearly), and the maturity value is the principal amount only.

---

## Changes Made

### 1. Response DTO Updates (`FDCalculationResponse.java`)

#### New Fields Added:
```java
String payout_freq       // MONTHLY, QUARTERLY, YEARLY (null if cumulative)
BigDecimal payout_amount // Interest paid per period (null if cumulative)
```

#### Complete Structure:
```java
public record FDCalculationResponse(
    BigDecimal maturity_value,
    String maturity_date,
    BigDecimal apy,
    BigDecimal effective_rate,
    String payout_freq,         // NEW
    BigDecimal payout_amount,   // NEW
    Long calc_id,
    Long result_id
) {}
```

### 2. Entity Updates (`FDCalculationResult.java`)

#### New Fields Added:
```java
@Column(length = 20)
private String payoutFreq;  // MONTHLY, QUARTERLY, YEARLY (null if cumulative)

@Column(precision = 20, scale = 4)
private BigDecimal payoutAmount;  // Interest paid per period (null if cumulative)
```

### 3. Service Logic Updates (`FDCalculatorServiceImpl.java`)

#### New Method: `calculatePeriodicPayout()`
Calculates the interest amount paid out at each payout period.

**Formula:**
```
Annual Interest = Principal × Rate
Interest Per Period = Annual Interest ÷ Periods Per Year
```

**Example:**
- Principal: ₹100,000
- Rate: 8%
- Payout Frequency: MONTHLY
- Periods Per Year: 12
- Interest Per Period: (100,000 × 0.08) ÷ 12 = ₹666.67

---

## Cumulative vs Non-Cumulative FDs

### Cumulative FD
Interest is compounded and paid at maturity along with principal.

**Request:**
```json
{
  "principal_amount": 100000,
  "tenure_value": 3,
  "tenure_unit": "YEARS",
  "cumulative": true,
  "payout_freq": null,
  "category1_id": "SENIOR",
  "product_code": "FD001"
}
```

**Response:**
```json
{
  "maturity_value": 132649.23,
  "maturity_date": "2028-10-10",
  "apy": 10.52,
  "effective_rate": 10.25,
  "payout_freq": null,
  "payout_amount": null,
  "calc_id": 1,
  "result_id": 1
}
```

**Explanation:**
- ✅ Interest compounded quarterly for 3 years
- ✅ Maturity value = Principal + All Interest
- ✅ `payout_freq` and `payout_amount` are NULL

---

### Non-Cumulative FD (Monthly Payout)

Interest is paid out monthly, maturity value is principal only.

**Request:**
```json
{
  "principal_amount": 100000,
  "tenure_value": 3,
  "tenure_unit": "YEARS",
  "cumulative": false,
  "payout_freq": "MONTHLY",
  "category1_id": "SENIOR",
  "product_code": "FD001"
}
```

**Response:**
```json
{
  "maturity_value": 100000.00,
  "maturity_date": "2028-10-10",
  "apy": 10.25,
  "effective_rate": 10.25,
  "payout_freq": "MONTHLY",
  "payout_amount": 854.17,
  "calc_id": 2,
  "result_id": 2
}
```

**Explanation:**
- ✅ Interest paid monthly: ₹854.17 per month
- ✅ Total interest over 3 years: 854.17 × 36 = ₹30,750
- ✅ Maturity value = Principal only (₹100,000)
- ✅ `payout_freq` = "MONTHLY"
- ✅ `payout_amount` = ₹854.17

**Cash Flow:**
```
Month 1:  +854.17 (interest)
Month 2:  +854.17 (interest)
...
Month 36: +854.17 (interest)
Maturity: +100,000.00 (principal)
```

---

### Non-Cumulative FD (Quarterly Payout)

Interest is paid out quarterly.

**Request:**
```json
{
  "principal_amount": 100000,
  "tenure_value": 2,
  "tenure_unit": "YEARS",
  "cumulative": false,
  "payout_freq": "QUARTERLY",
  "category1_id": "SENIOR",
  "product_code": "FD001"
}
```

**Response:**
```json
{
  "maturity_value": 100000.00,
  "maturity_date": "2027-10-10",
  "apy": 8.25,
  "effective_rate": 8.25,
  "payout_freq": "QUARTERLY",
  "payout_amount": 2062.50,
  "calc_id": 3,
  "result_id": 3
}
```

**Explanation:**
- ✅ Interest paid quarterly: ₹2,062.50 per quarter
- ✅ Total interest over 2 years: 2,062.50 × 8 = ₹16,500
- ✅ Maturity value = Principal only (₹100,000)

**Cash Flow:**
```
Quarter 1: +2,062.50 (interest)
Quarter 2: +2,062.50 (interest)
...
Quarter 8: +2,062.50 (interest)
Maturity:  +100,000.00 (principal)
```

---

### Non-Cumulative FD (Yearly Payout)

Interest is paid out annually.

**Request:**
```json
{
  "principal_amount": 100000,
  "tenure_value": 5,
  "tenure_unit": "YEARS",
  "cumulative": false,
  "payout_freq": "YEARLY",
  "category1_id": null,
  "product_code": "FD001"
}
```

**Response:**
```json
{
  "maturity_value": 100000.00,
  "maturity_date": "2030-10-10",
  "apy": 8.50,
  "effective_rate": 8.50,
  "payout_freq": "YEARLY",
  "payout_amount": 8500.00,
  "calc_id": 4,
  "result_id": 4
}
```

**Explanation:**
- ✅ Interest paid yearly: ₹8,500 per year
- ✅ Total interest over 5 years: 8,500 × 5 = ₹42,500
- ✅ Maturity value = Principal only (₹100,000)

**Cash Flow:**
```
Year 1:   +8,500.00 (interest)
Year 2:   +8,500.00 (interest)
Year 3:   +8,500.00 (interest)
Year 4:   +8,500.00 (interest)
Year 5:   +8,500.00 (interest)
Maturity: +100,000.00 (principal)
```

---

## Calculation Logic

### Payout Amount Calculation

```java
private BigDecimal calculatePeriodicPayout(
    BigDecimal principal, 
    BigDecimal ratePct, 
    int tenure, 
    String tenureUnit, 
    String payoutFreq
) {
    // 1. Convert rate to decimal
    BigDecimal rate = ratePct.movePointLeft(2);
    
    // 2. Convert tenure to years
    BigDecimal tenureInYears = toYears(tenure, tenureUnit);
    
    // 3. Get periods per year
    int periodsPerYear = switch (payoutFreq.toUpperCase()) {
        case "MONTHLY" -> 12;
        case "QUARTERLY" -> 4;
        case "YEARLY" -> 1;
    };
    
    // 4. Calculate annual interest
    BigDecimal annualInterest = principal.multiply(rate);
    
    // 5. Calculate interest per period
    BigDecimal interestPerPeriod = annualInterest.divide(
        new BigDecimal(periodsPerYear), 
        MathContext.DECIMAL64
    );
    
    return interestPerPeriod.setScale(4, RoundingMode.HALF_UP);
}
```

### Decision Logic in calculate() Method

```java
boolean isNonCumulative = req.cumulative() != null && !req.cumulative();

if (isNonCumulative) {
    // Set payout frequency (use payout_freq from request, or fallback to compounding_frequency)
    payoutFreq = req.payout_freq() != null ? req.payout_freq() : req.compounding_frequency();
    if (payoutFreq == null) payoutFreq = "YEARLY";
    
    // Calculate periodic payout
    payoutAmount = calculatePeriodicPayout(...);
    
    // Maturity value = Principal only
    maturityValue = req.principal_amount();
    
    // APY = Effective rate (no compounding)
    apy = effectiveRate;
} else {
    // Cumulative: compound interest and pay at maturity
    maturityValue = compoundMaturity(...);
    apy = calcAPY(...);
    
    // payout_freq and payout_amount remain null
}
```

---

## Payout Frequency Options

| Frequency | Value | Periods/Year | Example Payout for ₹100,000 @ 8% |
|-----------|-------|--------------|-----------------------------------|
| MONTHLY | "MONTHLY" | 12 | ₹666.67/month |
| QUARTERLY | "QUARTERLY" | 4 | ₹2,000.00/quarter |
| YEARLY | "YEARLY" | 1 | ₹8,000.00/year |

---

## Request/Response Examples

### Example 1: Compare Cumulative vs Non-Cumulative

**Same Parameters:**
- Principal: ₹100,000
- Tenure: 3 years
- Rate: 10.25% (with SENIOR benefit)
- Compounding: Quarterly

#### Cumulative FD:
```json
{
  "principal_amount": 100000,
  "tenure_value": 3,
  "tenure_unit": "YEARS",
  "interest_type": "COMPOUND",
  "compounding_frequency": "QUARTERLY",
  "cumulative": true,
  "category1_id": "SENIOR",
  "product_code": "FD001"
}
```

**Result:**
- Maturity: ₹135,228.45 (principal + interest)
- Total Earnings: ₹35,228.45
- Paid at maturity: Everything at once

#### Non-Cumulative FD (Quarterly Payout):
```json
{
  "principal_amount": 100000,
  "tenure_value": 3,
  "tenure_unit": "YEARS",
  "interest_type": "COMPOUND",
  "compounding_frequency": "QUARTERLY",
  "cumulative": false,
  "payout_freq": "QUARTERLY",
  "category1_id": "SENIOR",
  "product_code": "FD001"
}
```

**Result:**
- Maturity: ₹100,000 (principal only)
- Quarterly Payout: ₹2,562.50
- Total Payouts: ₹2,562.50 × 12 = ₹30,750
- Total Earnings: ₹30,750

**Comparison:**
- Cumulative earns MORE (₹35,228.45 vs ₹30,750) due to compounding
- Non-cumulative provides REGULAR INCOME (₹2,562.50 every quarter)

---

## Database Schema Changes

### Migration Required:

```sql
ALTER TABLE fd_calculation_result 
ADD COLUMN payout_freq VARCHAR(20) NULL,
ADD COLUMN payout_amount DECIMAL(20,4) NULL;
```

### Field Details:
- `payout_freq`: VARCHAR(20), nullable
  - Values: "MONTHLY", "QUARTERLY", "YEARLY", or NULL
  - NULL when cumulative = true
  
- `payout_amount`: DECIMAL(20,4), nullable
  - Precision: 20 digits total, 4 decimal places
  - NULL when cumulative = true

---

## Validation Rules

### Request Validation:

1. **If cumulative = false:**
   - `payout_freq` should be provided (MONTHLY, QUARTERLY, or YEARLY)
   - If not provided, falls back to `compounding_frequency`
   - If still null, defaults to "YEARLY"

2. **If cumulative = true:**
   - `payout_freq` is ignored (can be null)
   - Interest compounded based on `compounding_frequency`

3. **Field Consistency:**
   - `cumulative` field is optional (defaults to true if null)
   - `payout_freq` is optional (has fallback logic)

---

## Benefits of Non-Cumulative FDs

### For Customers:
✅ **Regular Income Stream** - Monthly/quarterly/yearly interest payments  
✅ **Fixed Income** - Predictable payout amounts  
✅ **Principal Protection** - Original investment returned at maturity  
✅ **Budget Planning** - Helps with monthly expenses  

### For Retirees/Senior Citizens:
✅ **Pension Supplement** - Regular interest acts as additional income  
✅ **Liquidity** - Access to earnings without breaking FD  
✅ **Compounding Benefits** - Can reinvest payouts elsewhere  

### Use Cases:
- 💰 Monthly living expenses
- 💰 Rent/mortgage payments
- 💰 Supplementing pension income
- 💰 Regular investment plans (SIPs with FD interest)

---

## Testing Scenarios

### Test Case 1: Non-Cumulative Monthly
```bash
curl -X POST http://localhost:8081/api/fd-calculator/calculate \
  -H "Content-Type: application/json" \
  -d '{
    "principal_amount": 100000,
    "tenure_value": 2,
    "tenure_unit": "YEARS",
    "interest_type": "COMPOUND",
    "compounding_frequency": "MONTHLY",
    "cumulative": false,
    "payout_freq": "MONTHLY",
    "category1_id": "SENIOR",
    "product_code": "FD001"
  }'
```

**Expected:**
- maturity_value = 100000 (principal only)
- payout_freq = "MONTHLY"
- payout_amount ≈ 687.50

### Test Case 2: Cumulative (No Payouts)
```bash
curl -X POST http://localhost:8081/api/fd-calculator/calculate \
  -H "Content-Type: application/json" \
  -d '{
    "principal_amount": 100000,
    "tenure_value": 2,
    "tenure_unit": "YEARS",
    "interest_type": "COMPOUND",
    "compounding_frequency": "QUARTERLY",
    "cumulative": true,
    "category1_id": "SENIOR",
    "product_code": "FD001"
  }'
```

**Expected:**
- maturity_value > 100000 (principal + interest)
- payout_freq = null
- payout_amount = null

### Test Case 3: Non-Cumulative Quarterly with Categories
```bash
curl -X POST http://localhost:8081/api/fd-calculator/calculate \
  -H "Content-Type: application/json" \
  -d '{
    "principal_amount": 50000,
    "tenure_value": 5,
    "tenure_unit": "YEARS",
    "interest_type": "COMPOUND",
    "compounding_frequency": "QUARTERLY",
    "cumulative": false,
    "payout_freq": "QUARTERLY",
    "category1_id": "SENIOR",
    "category2_id": "GOLD",
    "product_code": "FD001"
  }'
```

**Expected:**
- maturity_value = 50000
- payout_freq = "QUARTERLY"
- effective_rate = base + 0.75 (SR) + 1.0 (GOLD)
- payout_amount calculated with effective rate

---

## Logging

### Log Messages Added:

```java
// When non-cumulative FD detected
log.info("Non-cumulative FD: Payout freq={}, Payout amount={}, Maturity=Principal only", 
    payoutFreq, payoutAmount);

// When cumulative FD detected
log.info("Cumulative FD: Maturity value={}", maturityValue);

// During payout calculation
log.info("Non-cumulative payout calculation: Principal={}, Rate={}%, Tenure={} {}, " +
         "Payout freq={}, Periods per year={}, Total periods={}, Payout amount per period={}", 
         principal, ratePct, tenure, tenureUnit, payoutFreq, periodsPerYear, 
         totalPeriods, interestPerPeriod);
```

---

## Summary

### ✅ What Was Added:

1. **Response Fields:**
   - `payout_freq` - Frequency of interest payouts
   - `payout_amount` - Amount paid per period

2. **Entity Fields:**
   - `payoutFreq` - Stored in database for audit
   - `payoutAmount` - Stored in database for audit

3. **Business Logic:**
   - `calculatePeriodicPayout()` - Calculate payout amount
   - Conditional logic for cumulative vs non-cumulative
   - Maturity value = principal only for non-cumulative

4. **Documentation:**
   - Complete examples for all scenarios
   - Calculation formulas
   - Testing guidelines

### 🎯 Key Points:

- ✅ **Cumulative FDs:** Interest compounded, paid at maturity, higher returns
- ✅ **Non-Cumulative FDs:** Interest paid periodically, regular income, principal at maturity
- ✅ **Payout Frequency:** Monthly (12/year), Quarterly (4/year), Yearly (1/year)
- ✅ **Fallback Logic:** If payout_freq not provided, uses compounding_frequency
- ✅ **Database Ready:** New columns added with proper nullability

---

**Documentation Version:** 1.0  
**Date:** October 10, 2025  
**Status:** ✅ Ready for Testing
