# Product Configuration Integration

## Overview
This document describes the integration changes to automatically fetch `interestType` and `compoundingFrequency` from the Product & Pricing API instead of requiring them in the FD calculation request body.

## Changes Made

### 1. New DTO Created
**File**: `ProductDetailsDTO.java`
- Created a new DTO to capture product details from the Product & Pricing API
- Includes fields: `productId`, `productCode`, `productName`, `productType`, `currency`, `status`, `interestType`, `compoundingFrequency`

### 2. PricingApiClient Updated
**File**: `PricingApiClient.java`
- Added new endpoint: `getProductDetails(String productCode)`
- Maps to: `GET /api/products/{productCode}`
- Returns: `ProductDetailsDTO` with product configuration

### 3. MockPricingApiClient Updated
**File**: `MockPricingApiClient.java`
- Implemented `getProductDetails()` method for mock profile
- Returns mock data with:
  - `interestType`: "COMPOUND"
  - `compoundingFrequency`: "QUARTERLY"

### 4. FDCalculationRequest Updated
**File**: `FDCalculationRequest.java`
- Made `interest_type` optional (changed from `required = true` to `required = false`)
- Made `compounding_frequency` optional (already was optional)
- Updated descriptions to indicate values will be fetched from Product & Pricing API if not provided

### 5. FDCalculatorServiceImpl Updated
**File**: `FDCalculatorServiceImpl.java`
- Added import for `ProductDetailsDTO`
- Modified `calculate()` method to:
  1. Fetch product details from API: `pricingApiClient.getProductDetails(productCode)`
  2. Use values from product details if not provided in request:
     - `interestType = req.interest_type() ?? productDetails.getInterestType()`
     - `compoundingFrequency = req.compounding_frequency() ?? productDetails.getCompoundingFrequency()`
  3. Replace all references to `req.interest_type()` with `interestType` variable
  4. Replace all references to `req.compounding_frequency()` with `compoundingFrequency` variable
  5. Store the resolved values in the database

### 6. FDCalculatorController Updated
**File**: `FDCalculatorController.java`
- Updated API documentation to mention automatic fetching from Product & Pricing API
- Updated all Swagger examples to remove `interest_type` and `compounding_frequency` fields
- Added note that manual override is still possible

## API Behavior

### Request Flow
1. Client sends FD calculation request with `product_code`
2. Service fetches product details from `GET /api/products/{productCode}`
3. Service extracts `interestType` and `compoundingFrequency` from product details
4. If client provided these values in request, they override the product defaults
5. Service performs calculation using the resolved values

### Backward Compatibility
- ✅ **Fully backward compatible**
- Clients can still provide `interest_type` and `compounding_frequency` in request
- If provided, request values take precedence over product defaults
- If not provided, values are fetched from product configuration

### Example Requests

#### Minimal Request (Recommended)
```json
{
  "principal_amount": 100000,
  "tenure_value": 5,
  "tenure_unit": "YEARS",
  "currency_code": "INR",
  "category1_id": "SENIOR",
  "cumulative": true,
  "product_code": "FD001"
}
```
*Note: `interest_type` and `compounding_frequency` are automatically fetched*

#### Request with Override
```json
{
  "principal_amount": 100000,
  "tenure_value": 5,
  "tenure_unit": "YEARS",
  "interest_type": "SIMPLE",
  "compounding_frequency": "MONTHLY",
  "currency_code": "INR",
  "category1_id": "SENIOR",
  "cumulative": true,
  "product_code": "FD001"
}
```
*Note: Request values override product defaults*

## Product & Pricing API Requirements

The Product & Pricing API must include these fields in the response from `GET /api/products/{productCode}`:

```json
{
  "productCode": "FD001",
  "productName": "Fixed Deposit Product",
  "productType": "FIXED_DEPOSIT",
  "currency": "INR",
  "status": "ACTIVE",
  "interestType": "COMPOUND",
  "compoundingFrequency": "QUARTERLY"
}
```

### Field Descriptions
- **interestType**: `"SIMPLE"` or `"COMPOUND"`
- **compoundingFrequency**: `"DAILY"`, `"MONTHLY"`, `"QUARTERLY"`, or `"YEARLY"`

## Benefits

1. **Centralized Configuration**: Product configuration managed in one place
2. **Reduced Client Complexity**: Clients don't need to know product-specific settings
3. **Consistency**: Same product always uses same interest calculation method
4. **Flexibility**: Override capability maintained for special cases
5. **Better UX**: Simpler API requests with fewer required fields

## Testing

### With Mock Profile
Run with `spring.profiles.active=mock`:
- Uses `MockPricingApiClient`
- Returns hardcoded values: `COMPOUND` interest type, `QUARTERLY` compounding

### With Production Profile
Run with `spring.profiles.active=prod`:
- Uses real `PricingApiClient` with Feign
- Fetches actual product details from Product & Pricing API
- Configure `pricing.api.url` in `application-prod.yml`

## Error Handling

If product details cannot be fetched or don't contain required fields:
- Service logs the error
- Throws `IllegalArgumentException` with descriptive message
- Returns HTTP 400 Bad Request to client

## Database Impact

No schema changes required. The `FDCalculationInput` table already has:
- `interest_type` column
- `compounding_frequency` column

These columns now store the resolved values (from either request or product details).

## Migration Notes

For existing clients:
1. No changes required immediately
2. Can continue sending `interest_type` and `compounding_frequency`
3. Gradually migrate to simplified requests
4. Remove these fields when ready

## Date
Created: October 15, 2025
