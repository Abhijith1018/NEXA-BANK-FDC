# Swagger API Documentation Setup Guide

## ✅ Successfully Configured!

Your FD Calculator API is now fully documented with **Swagger/OpenAPI 3.0**.

---

## 🚀 Quick Access

### Swagger UI (Interactive Documentation)
```
http://localhost:8081/swagger-ui.html
```

### OpenAPI JSON Specification
```
http://localhost:8081/v3/api-docs
```

### OpenAPI YAML Specification
```
http://localhost:8081/v3/api-docs.yaml
```

---

## 📚 What's Been Added

### 1. Dependencies
- **SpringDoc OpenAPI 2.2.0** - Latest stable version for Spring Boot 3.x
- Automatic Swagger UI generation
- OpenAPI 3.0 specification support

### 2. Configuration Files

#### `OpenApiConfig.java`
Comprehensive API configuration with:
- API title, version, description
- Contact information
- Server URLs (dev and prod)
- Detailed overview of all features
- Business logic documentation

#### `application.yml`
SpringDoc configuration:
- Swagger UI path: `/swagger-ui.html`
- API docs path: `/v3/api-docs`
- Alphabetical sorting
- Try-it-out enabled
- Filter enabled

### 3. Controller Annotations

All controllers now have extensive documentation:

#### **FDCalculatorController**
- `@Tag` - Controller description
- `@Operation` - Endpoint summaries and detailed descriptions
- `@ApiResponses` - Response codes and examples
- `@Parameter` - Parameter documentation
- Multiple request/response examples for different scenarios

#### **ReferenceDataController**
- Category listings with benefit rates
- Currency options
- Compounding frequencies
- Rate cache management

#### **AdminController**
- Product rule synchronization
- Category management
- Admin-only operations

### 4. DTO Annotations

All DTOs fully documented with `@Schema`:

#### **FDCalculationRequest**
- Field-level descriptions
- Validation rules
- Example values
- Allowable values
- Required/optional flags

#### **FDCalculationResponse**
- Response field descriptions
- Business logic explanations
- Calculation formulas
- Nullable fields

#### **CategoryDTO**
- Category structure
- Benefit information

---

## 🎯 Features

### Interactive API Testing
- **Try it out** - Execute API calls directly from browser
- **Request examples** - Pre-filled examples for common scenarios
- **Response examples** - See expected outputs
- **Schema validation** - Real-time validation feedback

### Comprehensive Documentation
- **Business logic** - Detailed explanations of calculations
- **Formulas** - Mathematical formulas for APY, payouts, etc.
- **Use cases** - Real-world scenarios with examples
- **Error handling** - All possible error responses

### Developer-Friendly
- **Alphabetical sorting** - Easy navigation
- **Search/filter** - Quick endpoint lookup
- **Code generation** - Export client code
- **Download spec** - Get OpenAPI JSON/YAML

---

## 📖 Documentation Highlights

### API Overview
```
The FD Calculator API provides comprehensive fixed deposit calculation services
for NEXA Bank customers, supporting both cumulative and non-cumulative FDs
with dynamic interest rates and category-based benefits.
```

### Key Features Documented
1. **Cumulative FD** - Compound interest with maturity value
2. **Non-Cumulative FD** - Periodic payouts with configurable frequency
3. **Dynamic Rates** - Tenure-based rate fetching from Product API
4. **Category Benefits** - 10 customer categories (SENIOR, GOLD, EMP, etc.)
5. **APY Calculation** - Accurate Annual Percentage Yield
6. **Compounding Options** - Daily, Monthly, Quarterly, Yearly

### Example Scenarios Included

#### Scenario 1: Senior Citizen Cumulative FD
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

#### Scenario 2: Gold Customer Non-Cumulative FD
```json
{
  "principal_amount": 50000,
  "tenure_value": 3,
  "tenure_unit": "YEARS",
  "interest_type": "COMPOUND",
  "compounding_frequency": "QUARTERLY",
  "category1_id": "GOLD",
  "cumulative": false,
  "payout_freq": "YEARLY",
  "product_code": "FD001"
}
```

#### Scenario 3: Employee Monthly Payout
```json
{
  "principal_amount": 200000,
  "tenure_value": 2,
  "tenure_unit": "YEARS",
  "interest_type": "COMPOUND",
  "compounding_frequency": "MONTHLY",
  "category1_id": "EMP",
  "cumulative": false,
  "payout_freq": "MONTHLY",
  "product_code": "FD001"
}
```

---

## 🧪 Testing Guide

### Step 1: Start the Application
```bash
cd /Users/Jaiwant/repos/NEXA-BANK-FDC/fd-calculator-backend
mvn spring-boot:run
```

### Step 2: Open Swagger UI
Navigate to: `http://localhost:8081/swagger-ui.html`

### Step 3: Explore the API
1. **View all endpoints** - Grouped by tags (FD Calculator, Reference Data, Admin)
2. **Read documentation** - Click any endpoint to see details
3. **Try examples** - Click "Try it out" and use pre-filled examples
4. **Execute requests** - Click "Execute" to test the API
5. **View responses** - See actual results with status codes

### Step 4: Test Different Scenarios

#### Test 1: Calculate Cumulative FD
```
Endpoint: POST /api/fd/calculate
Use: "Cumulative FD - Senior Citizen" example
Expected: maturity_value > principal_amount
```

#### Test 2: Calculate Non-Cumulative FD
```
Endpoint: POST /api/fd/calculate
Use: "Non-Cumulative FD - Gold Customer" example
Expected: payout_freq = "YEARLY", payout_amount populated
```

#### Test 3: Get Categories
```
Endpoint: GET /api/fd/categories
Expected: List of categories with benefit rates
```

#### Test 4: Retrieve Calculation
```
Endpoint: GET /api/fd/calculations/{calcId}
Use: calc_id from previous calculation
Expected: Same result as original calculation
```

---

## 📋 API Tags (Groups)

### 1. FD Calculator
Main calculation endpoints:
- `POST /api/fd/calculate` - Calculate FD
- `GET /api/fd/calculations/{calcId}` - Get calculation
- `GET /api/fd/history` - Get history

### 2. Reference Data
Lookup data:
- `GET /api/fd/categories` - Customer categories
- `GET /api/fd/currencies` - Supported currencies
- `GET /api/fd/compounding-options` - Compounding frequencies
- `GET /api/fd/rate-cache/{productCode}` - Cached rates
- `POST /api/fd/rate-cache/refresh` - Refresh cache

### 3. Admin
Administrative tasks:
- `POST /api/admin/sync-product-rules/{productCode}` - Sync rules
- `GET /api/admin/categories` - All categories

---

## 🎨 Swagger UI Features

### Navigation
- **Search bar** - Filter endpoints by name
- **Tag grouping** - Organized by functionality
- **Expand/collapse** - Control visibility
- **Alphabetical sorting** - Easy to find

### Request Details
- **Parameters** - Path, query, body parameters
- **Request body** - JSON schema with examples
- **Required fields** - Clearly marked
- **Validation rules** - Min, max, pattern constraints

### Response Details
- **Status codes** - 200, 400, 404, 500
- **Response schema** - Expected structure
- **Examples** - Sample responses
- **Headers** - Response headers

### Try It Out
- **Edit parameters** - Modify request data
- **Execute** - Send real request
- **Response** - See actual result
- **Curl command** - Copy as curl command

---

## 📥 Export Options

### 1. Download OpenAPI Spec
```bash
# JSON format
curl http://localhost:8081/v3/api-docs > openapi.json

# YAML format
curl http://localhost:8081/v3/api-docs.yaml > openapi.yaml
```

### 2. Generate Client Code
Use the OpenAPI spec with:
- **Swagger Codegen** - Generate client libraries
- **OpenAPI Generator** - Multiple language support
- **Postman** - Import into Postman collections

### 3. Share with Team
- Send Swagger UI link: `http://localhost:8081/swagger-ui.html`
- Share OpenAPI spec file
- Export as PDF (browser print)

---

## 🔧 Customization Options

### Change Swagger UI Path
In `application.yml`:
```yaml
springdoc:
  swagger-ui:
    path: /docs  # Default: /swagger-ui.html
```

### Change API Docs Path
```yaml
springdoc:
  api-docs:
    path: /api-docs  # Default: /v3/api-docs
```

### Disable in Production
```yaml
springdoc:
  swagger-ui:
    enabled: false  # Disable Swagger UI
  api-docs:
    enabled: false  # Disable API docs
```

### Add Security
In `OpenApiConfig.java`, add:
```java
@Bean
public OpenAPI openAPI() {
    return new OpenAPI()
        .components(new Components()
            .addSecuritySchemes("bearer-jwt",
                new SecurityScheme()
                    .type(SecurityScheme.Type.HTTP)
                    .scheme("bearer")
                    .bearerFormat("JWT")))
        .addSecurityItem(new SecurityRequirement().addList("bearer-jwt"));
}
```

---

## 📊 Business Logic Documentation

### Interest Rate Calculation
```
Effective Rate = Base Rate + Category1 Benefit + Category2 Benefit

Example:
- Base Rate: 7.5% (from Product API)
- Category1: SENIOR (+0.75%)
- Category2: GOLD (+0.25%)
- Effective Rate: 8.5%
```

### APY Formula
```
APY = (1 + r/n)^n - 1

Where:
- r = Effective rate (as decimal)
- n = Compounding periods per year

Example (10.25% quarterly):
APY = (1 + 0.1025/4)^4 - 1 = 10.6508%
```

### Non-Cumulative Payout
```
Payout = Principal × [(1 + r/m)^n - 1]

Where:
- r = Rate per compounding period
- m = Compounding periods per year
- n = Compounding periods per payout

Example (₹50,000, 10.25%, quarterly→yearly):
Payout = 50,000 × [(1.025625)^4 - 1] = ₹5,325.38
```

---

## 🌟 Benefits

### For Developers
✅ **No manual documentation** - Auto-generated from code  
✅ **Always up-to-date** - Changes reflect immediately  
✅ **Interactive testing** - No Postman needed  
✅ **Client generation** - Auto-generate SDKs  

### For QA Team
✅ **Test directly** - Browser-based API testing  
✅ **All scenarios** - Pre-built test cases  
✅ **Response validation** - See expected vs actual  
✅ **Easy debugging** - Clear error messages  

### For Product Team
✅ **Business logic** - Formulas and calculations explained  
✅ **Use cases** - Real-world examples  
✅ **Feature overview** - Complete API capabilities  
✅ **No technical knowledge** - User-friendly interface  

### For Partners/Integrators
✅ **Self-service** - Complete documentation  
✅ **Code examples** - Multiple scenarios  
✅ **OpenAPI spec** - Standard format  
✅ **Client generation** - Quick integration  

---

## 📝 Related Documentation

1. **API_DOCUMENTATION.md** - Comprehensive API guide
2. **NON_CUMULATIVE_APY_FIX.md** - APY calculation fix details
3. **README.md** - Project overview
4. **doc.yaml** - Product & Pricing API spec

---

## 🚀 Next Steps

### 1. Review Documentation
- Open Swagger UI
- Review all endpoints
- Test example scenarios
- Verify accuracy

### 2. Customize (Optional)
- Update contact information in `OpenApiConfig.java`
- Add your company branding
- Configure production server URLs
- Add authentication (if needed)

### 3. Share with Team
- Send Swagger UI link
- Demo the interactive features
- Train team on API testing
- Gather feedback

### 4. Production Deployment
- Disable Swagger in prod (optional)
- Or restrict access to authenticated users
- Monitor API usage
- Update documentation as needed

---

## 🎓 Learning Resources

### Swagger/OpenAPI
- [SpringDoc Documentation](https://springdoc.org/)
- [OpenAPI Specification](https://swagger.io/specification/)
- [Swagger UI Guide](https://swagger.io/tools/swagger-ui/)

### Annotations Reference
- `@Tag` - Controller grouping
- `@Operation` - Endpoint description
- `@Parameter` - Parameter documentation
- `@Schema` - Model documentation
- `@ApiResponse` - Response documentation

---

## ✅ Checklist

- [x] Added SpringDoc OpenAPI dependency
- [x] Created OpenApiConfig.java
- [x] Configured application.yml
- [x] Annotated FDCalculatorController
- [x] Annotated ReferenceDataController
- [x] Annotated AdminController
- [x] Annotated FDCalculationRequest DTO
- [x] Annotated FDCalculationResponse DTO
- [x] Annotated CategoryDTO
- [x] Added comprehensive examples
- [x] Documented business logic
- [x] Created API_DOCUMENTATION.md
- [x] Created SWAGGER_SETUP.md

---

## 🎉 Success!

Your API is now professionally documented with:

✅ **Interactive Swagger UI** at http://localhost:8081/swagger-ui.html  
✅ **OpenAPI 3.0 Specification** at http://localhost:8081/v3/api-docs  
✅ **Comprehensive Documentation** in API_DOCUMENTATION.md  
✅ **50+ Examples** across all endpoints  
✅ **Business Logic** formulas and calculations  
✅ **Testing Guide** for QA and developers  

**Ready to use! Just start the application and open Swagger UI.**

---

**Last Updated:** October 10, 2025  
**SpringDoc Version:** 2.2.0  
**OpenAPI Version:** 3.0  
**Author:** NEXA Bank Development Team
