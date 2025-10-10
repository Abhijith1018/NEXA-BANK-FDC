# FD Calculator API Documentation

## Overview

The **NEXA Bank Fixed Deposit Calculator API** is a comprehensive RESTful service for calculating fixed deposit returns with support for both cumulative and non-cumulative FDs.

## Quick Start

### Access Points

- **Swagger UI**: http://localhost:8081/swagger-ui.html
- **OpenAPI JSON**: http://localhost:8081/v3/api-docs
- **Base URL**: http://localhost:8081/api/fd

### Quick Example

```bash
curl -X POST http://localhost:8081/api/fd/calculate \
  -H "Content-Type: application/json" \
  -d '{
    "principal_amount": 100000,
    "tenure_value": 5,
    "tenure_unit": "YEARS",
    "interest_type": "COMPOUND",
    "compounding_frequency": "QUARTERLY",
    "category1_id": "SENIOR",
    "cumulative": true,
    "product_code": "FD001"
  }'
```

---

## API Endpoints

### 1. FD Calculator Endpoints

#### POST `/api/fd/calculate`
Calculate Fixed Deposit returns

**Request Body:**
```json
{
  "principal_amount": 100000,
  "tenure_value": 5,
  "tenure_unit": "YEARS",
  "interest_type": "COMPOUND",
  "compounding_frequency": "QUARTERLY",
  "currency_code": "INR",
  "category1_id": "SENIOR",
  "category2_id": "GOLD",
  "cumulative": true,
  "payout_freq": "YEARLY",
  "product_code": "FD001"
}
```

**Response:**
```json
{
  "maturity_value": 164361.50,
  "maturity_date": "2030-10-10",
  "apy": 10.6508,
  "effective_rate": 10.2500,
  "payout_freq": null,
  "payout_amount": null,
  "calc_id": 123,
  "result_id": 123
}
```

---

#### GET `/api/fd/calculations/{calcId}`
Retrieve a previously calculated FD result

**Path Parameters:**
- `calcId` (Long): Calculation ID

**Example:**
```bash
curl http://localhost:8081/api/fd/calculations/123
```

**Response:** Same as calculate endpoint

---

#### GET `/api/fd/history`
Get list of all calculation IDs

**Example:**
```bash
curl http://localhost:8081/api/fd/history
```

**Response:**
```json
[101, 102, 103, 104, 105]
```

---

### 2. Reference Data Endpoints

#### GET `/api/fd/categories`
Get all customer categories

**Response:**
```json
[
  {
    "category_id": 1,
    "category_name": "Senior Citizen",
    "additional_percentage": 0.75
  },
  {
    "category_id": 2,
    "category_name": "Gold Customer",
    "additional_percentage": 0.25
  }
]
```

---

#### GET `/api/fd/currencies`
Get supported currency codes

**Response:**
```json
["INR", "JPY", "AED"]
```

---

#### GET `/api/fd/compounding-options`
Get available compounding frequencies

**Response:**
```json
["DAILY", "MONTHLY", "QUARTERLY", "YEARLY"]
```

---

#### POST `/api/fd/rate-cache/refresh`
Manually refresh rate cache for a product

**Query Parameters:**
- `productCode` (String): Product code (e.g., "FD001")

**Example:**
```bash
curl -X POST "http://localhost:8081/api/fd/rate-cache/refresh?productCode=FD001"
```

**Response:**
```
Refreshed FD001
```

---

#### GET `/api/fd/rate-cache/{productCode}`
Get cached base rate for a product

**Path Parameters:**
- `productCode` (String): Product code

**Example:**
```bash
curl http://localhost:8081/api/fd/rate-cache/FD001
```

**Response:**
```json
7.50
```

---

### 3. Admin Endpoints

#### POST `/api/admin/sync-product-rules/{productCode}`
Sync product rules from Product & Pricing API

**Path Parameters:**
- `productCode` (String): Product code

**Example:**
```bash
curl -X POST http://localhost:8081/api/admin/sync-product-rules/FD001
```

**Response:**
```json
{
  "status": "success",
  "message": "Successfully synced product rules for FD001"
}
```

---

#### GET `/api/admin/categories`
Get all categories from database (admin view)

**Example:**
```bash
curl http://localhost:8081/api/admin/categories
```

**Response:** Same structure as `/api/fd/categories`

---

## Data Models

### FDCalculationRequest

| Field | Type | Required | Description | Example |
|-------|------|----------|-------------|---------|
| `principal_amount` | BigDecimal | Yes | Principal amount to deposit | 100000 |
| `tenure_value` | Integer | Yes | Tenure numeric value | 5 |
| `tenure_unit` | String | Yes | DAYS, MONTHS, YEARS | "YEARS" |
| `interest_type` | String | Yes | SIMPLE, COMPOUND | "COMPOUND" |
| `compounding_frequency` | String | No* | DAILY, MONTHLY, QUARTERLY, YEARLY | "QUARTERLY" |
| `currency_code` | String | No | INR, JPY, AED | "INR" |
| `category1_id` | String | No | Primary category code | "SENIOR" |
| `category2_id` | String | No | Secondary category code | "GOLD" |
| `cumulative` | Boolean | No | true=cumulative, false=non-cumulative | true |
| `payout_freq` | String | No** | MONTHLY, QUARTERLY, YEARLY | "YEARLY" |
| `product_code` | String | Yes | Product code | "FD001" |

*Required for COMPOUND interest type  
**Required when cumulative=false

---

### FDCalculationResponse

| Field | Type | Description | Example |
|-------|------|-------------|---------|
| `maturity_value` | BigDecimal | Final amount at maturity | 164361.50 |
| `maturity_date` | String | Maturity date (YYYY-MM-DD) | "2030-10-10" |
| `apy` | BigDecimal | Annual Percentage Yield | 10.6508 |
| `effective_rate` | BigDecimal | Effective interest rate | 10.2500 |
| `payout_freq` | String | Payout frequency (non-cumulative only) | "QUARTERLY" |
| `payout_amount` | BigDecimal | Payout per period (non-cumulative only) | 5325.38 |
| `calc_id` | Long | Calculation ID | 123 |
| `result_id` | Long | Result ID | 123 |

---

## Business Logic

### FD Types

#### Cumulative FD
- Interest is compounded and reinvested
- Customer receives full amount at maturity
- **Formula:** `A = P(1 + r/n)^(nt)`
- **Returns:** `maturity_value`, `apy`, `effective_rate`

#### Non-Cumulative FD
- Interest is paid out periodically
- Principal remains constant
- **Formula:** `Payout = P × [(1 + r/m)^n - 1]`
- **Returns:** `payout_freq`, `payout_amount`, `maturity_value` (= principal)

---

### Interest Rate Calculation

#### Base Rate (Tenure-Based)
Interest rates are fetched from Product & Pricing API based on tenure:

| Tenure Range | Rate Code | Example Rate |
|--------------|-----------|--------------|
| 0-12 months | INT12M001 | 6.5% |
| 12-24 months | INT24M001 | 7.0% |
| 24-36 months | INT36M001 | 7.5% |
| 36+ months | INT60M001 | 8.0% |

#### Category Benefits
Additional interest rates based on customer categories:

| Category | Code | Benefit | Description |
|----------|------|---------|-------------|
| Senior Citizen | SENIOR | +0.75% | Age 60+ |
| Junior Citizen | JR | +0.50% | Age < 18 |
| Divyang | DY | +1.25% | Differently-abled |
| Employee | EMP | +1.00% | Bank employees |
| Platinum | PLAT | +0.35% | Premium customers |
| Gold | GOLD | +0.25% | Premium customers |
| Silver | SILVER | +0.15% | Premium customers |

#### Effective Rate Formula
```
Effective Rate = Base Rate + Category1 Benefit + Category2 Benefit
```

**Example:**
- Base Rate: 7.5% (36+ months)
- Category1: SENIOR (+0.75%)
- Category2: GOLD (+0.25%)
- **Effective Rate: 8.5%**

---

### APY (Annual Percentage Yield)

APY accounts for compounding frequency and shows the actual annual return.

**Formula:**
```
APY = (1 + r/n)^n - 1

Where:
- r = Effective rate (as decimal)
- n = Compounding periods per year
```

**Example:** 10.25% rate with quarterly compounding
```
APY = (1 + 0.1025/4)^4 - 1
    = (1.025625)^4 - 1
    = 1.106508 - 1
    = 0.106508
    = 10.6508%
```

#### APY by Compounding Frequency

| Compounding | Periods/Year (n) |
|-------------|------------------|
| DAILY | 365 |
| MONTHLY | 12 |
| QUARTERLY | 4 |
| YEARLY | 1 |

**Impact of Compounding on APY (10% rate):**

| Frequency | APY |
|-----------|-----|
| DAILY | 10.52% |
| MONTHLY | 10.47% |
| QUARTERLY | 10.38% |
| YEARLY | 10.00% |

---

### Non-Cumulative Payout Calculation

For non-cumulative FDs, interest is calculated with compounding but paid out periodically.

**Formula:**
```
Payout = Principal × [(1 + r/m)^n - 1]

Where:
- r = Rate per compounding period
- m = Compounding periods per year
- n = Compounding periods per payout period
```

**Example:** ₹50,000, 10.25% rate, quarterly compounding, yearly payout
```
Rate per quarter: 10.25% / 4 = 2.5625%
Periods per year: 4

Payout = 50,000 × [(1.025625)^4 - 1]
       = 50,000 × [1.106508 - 1]
       = 50,000 × 0.106508
       = ₹5,325.38
```

---

## Example Use Cases

### Use Case 1: Senior Citizen - Cumulative FD

**Scenario:**
- 65-year-old customer
- Invests ₹1,00,000 for 5 years
- Wants maximum returns

**Request:**
```json
{
  "principal_amount": 100000,
  "tenure_value": 5,
  "tenure_unit": "YEARS",
  "interest_type": "COMPOUND",
  "compounding_frequency": "QUARTERLY",
  "category1_id": "SENIOR",
  "cumulative": true,
  "product_code": "FD001"
}
```

**Expected Result:**
- Base rate: ~8.0% (60 months)
- SENIOR benefit: +0.75%
- Effective rate: 8.75%
- APY: ~9.06% (quarterly compounding)
- Maturity value: ~₹1,53,000

---

### Use Case 2: Employee - Non-Cumulative with Monthly Income

**Scenario:**
- Bank employee
- Invests ₹2,00,000 for 3 years
- Needs monthly income

**Request:**
```json
{
  "principal_amount": 200000,
  "tenure_value": 3,
  "tenure_unit": "YEARS",
  "interest_type": "COMPOUND",
  "compounding_frequency": "MONTHLY",
  "category1_id": "EMP",
  "cumulative": false,
  "payout_freq": "MONTHLY",
  "product_code": "FD001"
}
```

**Expected Result:**
- Base rate: ~7.5% (36 months)
- EMP benefit: +1.0%
- Effective rate: 8.5%
- APY: ~8.84% (monthly compounding)
- Monthly payout: ~₹1,417

---

### Use Case 3: Divyang Gold Customer - Quarterly Payout

**Scenario:**
- Differently-abled gold customer
- Invests ₹50,000 for 5 years
- Wants quarterly income

**Request:**
```json
{
  "principal_amount": 50000,
  "tenure_value": 5,
  "tenure_unit": "YEARS",
  "interest_type": "COMPOUND",
  "compounding_frequency": "QUARTERLY",
  "category1_id": "DY",
  "category2_id": "GOLD",
  "cumulative": false,
  "payout_freq": "QUARTERLY",
  "product_code": "FD001"
}
```

**Expected Result:**
- Base rate: ~8.0% (60 months)
- DY benefit: +1.25%
- GOLD benefit: +0.25%
- Effective rate: 9.5%
- APY: ~9.84% (quarterly compounding)
- Quarterly payout: ~₹1,213

---

## Error Handling

### HTTP Status Codes

| Code | Meaning | Cause |
|------|---------|-------|
| 200 | Success | Request completed successfully |
| 400 | Bad Request | Invalid parameters or validation failed |
| 404 | Not Found | Calculation ID not found |
| 500 | Server Error | Product API unavailable or internal error |

### Common Error Responses

#### Validation Error (400)
```json
{
  "timestamp": "2025-10-10T10:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed for field 'principal_amount': must be greater than 0.01",
  "path": "/api/fd/calculate"
}
```

#### Product API Error (500)
```json
{
  "timestamp": "2025-10-10T10:30:00",
  "status": 500,
  "error": "Internal Server Error",
  "message": "Failed to fetch interest rates from Product & Pricing API",
  "path": "/api/fd/calculate"
}
```

---

## Testing with Swagger UI

1. **Open Swagger UI:** http://localhost:8081/swagger-ui.html

2. **Navigate to endpoint:** Click on "FD Calculator" tag

3. **Try it out:** Click "Try it out" button

4. **Enter parameters:** Use provided examples or customize

5. **Execute:** Click "Execute" button

6. **View response:** See results in Response Body section

### Tips for Testing

- Use **Example 1** for cumulative FD testing
- Use **Example 2** for non-cumulative FD testing
- Try different categories to see benefit rates
- Compare APY with different compounding frequencies

---

## Integration Guide

### Frontend Integration

```javascript
// Calculate FD
async function calculateFD(params) {
  const response = await fetch('http://localhost:8081/api/fd/calculate', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(params)
  });
  return await response.json();
}

// Usage
const result = await calculateFD({
  principal_amount: 100000,
  tenure_value: 5,
  tenure_unit: "YEARS",
  interest_type: "COMPOUND",
  compounding_frequency: "QUARTERLY",
  category1_id: "SENIOR",
  cumulative: true,
  product_code: "FD001"
});

console.log(`Maturity Value: ${result.maturity_value}`);
console.log(`APY: ${result.apy}%`);
```

### Backend Integration

```java
// Using RestTemplate
@Service
public class FDIntegrationService {
    
    @Autowired
    private RestTemplate restTemplate;
    
    public FDCalculationResponse calculateFD(FDCalculationRequest request) {
        String url = "http://localhost:8081/api/fd/calculate";
        return restTemplate.postForObject(url, request, FDCalculationResponse.class);
    }
}
```

---

## Performance Considerations

### Rate Caching
- Interest rates are cached in MySQL database
- Cache refreshes automatically via scheduler
- Manual refresh available via `/rate-cache/refresh` endpoint
- Reduces API calls to Product & Pricing service

### Database Indexing
- Calculation IDs indexed for fast retrieval
- Product codes indexed for rate cache lookup

### Best Practices
- Use rate cache for frequent calculations
- Batch category sync during off-peak hours
- Monitor Product & Pricing API availability

---

## Security Considerations

### Production Recommendations

1. **Add Authentication:**
   ```java
   @PreAuthorize("hasRole('USER')")
   public FDCalculationResponse calculate(...)
   ```

2. **Restrict Admin Endpoints:**
   ```java
   @PreAuthorize("hasRole('ADMIN')")
   public ResponseEntity<Map<String, String>> syncProductRules(...)
   ```

3. **Rate Limiting:**
   - Implement rate limiting on public endpoints
   - Prevent abuse of calculation endpoint

4. **Input Validation:**
   - Already implemented with Jakarta Validation
   - Additional business rule validation in service layer

5. **HTTPS Only:**
   - Enable SSL/TLS for production
   - Redirect HTTP to HTTPS

---

## Maintenance

### Monitoring

**Health Checks:**
```bash
# Check if service is running
curl http://localhost:8081/actuator/health

# Check cache status
curl http://localhost:8081/api/fd/rate-cache/FD001
```

**Logs:**
- Monitor application logs for errors
- Check Product API integration failures
- Track calculation volume

### Regular Tasks

1. **Daily:**
   - Verify Product API connectivity
   - Check rate cache freshness

2. **Weekly:**
   - Review calculation volume
   - Analyze error rates

3. **Monthly:**
   - Sync product rules manually
   - Verify category configurations

---

## Support

### Documentation Resources
- **Swagger UI:** http://localhost:8081/swagger-ui.html
- **OpenAPI Spec:** http://localhost:8081/v3/api-docs
- **This Guide:** API_DOCUMENTATION.md

### Contact
- **API Support:** support@nexabank.com
- **Technical Issues:** tech@nexabank.com

---

## Changelog

### Version 1.0.0 (October 2025)
- ✅ Initial release
- ✅ Cumulative FD calculation
- ✅ Non-cumulative FD with periodic payouts
- ✅ Category-based benefits (10 categories)
- ✅ Dynamic interest rate fetching
- ✅ APY calculation with compounding
- ✅ Comprehensive Swagger documentation
- ✅ Rate caching for performance
- ✅ Product rule synchronization

### Recent Fixes
- ✅ Fixed non-cumulative payout calculation (compound interest)
- ✅ Fixed APY calculation for non-cumulative FDs
- ✅ Added comprehensive API documentation

---

**Last Updated:** October 10, 2025  
**API Version:** 1.0.0  
**Author:** NEXA Bank Development Team
