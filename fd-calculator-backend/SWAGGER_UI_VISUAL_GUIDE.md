# Swagger UI Visual Guide

## What You'll See When You Open Swagger UI

### Homepage: http://localhost:8081/swagger-ui.html

```
╔══════════════════════════════════════════════════════════════════════════╗
║                                                                          ║
║  NEXA Bank - Fixed Deposit Calculator API                    v1.0.0    ║
║  ────────────────────────────────────────────────────────────────────   ║
║                                                                          ║
║  The FD Calculator API provides comprehensive fixed deposit             ║
║  calculation services for NEXA Bank customers.                          ║
║                                                                          ║
║  [Servers: Development Server ▼]                                        ║
║                                                                          ║
║  [Filter by tags _______________] [/v3/api-docs]                        ║
║                                                                          ║
╚══════════════════════════════════════════════════════════════════════════╝
```

---

## Section 1: FD Calculator (3 endpoints)

```
┌─────────────────────────────────────────────────────────────────────────┐
│ ▼ FD Calculator                                                         │
│   Fixed Deposit calculation endpoints for both cumulative and          │
│   non-cumulative FDs                                                    │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                         │
│ ▶ POST /api/fd/calculate                                               │
│   Calculate Fixed Deposit returns                                      │
│                                                                         │
│ ▶ GET /api/fd/calculations/{calcId}                                    │
│   Get calculation by ID                                                │
│                                                                         │
│ ▶ GET /api/fd/history                                                  │
│   Get calculation history                                              │
│                                                                         │
└─────────────────────────────────────────────────────────────────────────┘
```

### When You Expand: POST /api/fd/calculate

```
┌─────────────────────────────────────────────────────────────────────────┐
│ ▼ POST /api/fd/calculate                                               │
│   Calculate Fixed Deposit returns                                      │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                         │
│   Calculate Fixed Deposit maturity value, interest, and APY based on   │
│   provided parameters.                                                  │
│                                                                         │
│   Supports two FD types:                                               │
│   1. Cumulative FD (cumulative=true)                                   │
│      - Interest is compounded and reinvested                           │
│      - Returns maturity_value with accumulated interest                │
│                                                                         │
│   2. Non-Cumulative FD (cumulative=false)                              │
│      - Interest is paid out periodically                               │
│      - Returns payout_freq and payout_amount                           │
│                                                                         │
│   [Try it out]                                                         │
│                                                                         │
│   Request body *                                                        │
│   ┌───────────────────────────────────────────────────────────────┐   │
│   │ [Example Value ▼] [Schema ▼]                                  │   │
│   │                                                                │   │
│   │ Cumulative FD - Senior Citizen ▼                              │   │
│   │ Non-Cumulative FD - Gold Customer ▼                           │   │
│   │ Employee FD - Monthly Payout ▼                                │   │
│   │                                                                │   │
│   │ {                                                              │   │
│   │   "principal_amount": 100000,                                 │   │
│   │   "tenure_value": 5,                                          │   │
│   │   "tenure_unit": "YEARS",                                     │   │
│   │   "interest_type": "COMPOUND",                                │   │
│   │   "compounding_frequency": "QUARTERLY",                       │   │
│   │   "currency_code": "INR",                                     │   │
│   │   "category1_id": "SENIOR",                                   │   │
│   │   "category2_id": "GOLD",                                     │   │
│   │   "cumulative": true,                                         │   │
│   │   "product_code": "FD001"                                     │   │
│   │ }                                                              │   │
│   └───────────────────────────────────────────────────────────────┘   │
│                                                                         │
│   [Execute]                                                            │
│                                                                         │
│   ─────────────────────────────────────────────────────────────        │
│                                                                         │
│   Responses                                                             │
│                                                                         │
│   ▼ 200 Calculation successful                                         │
│     Example Value:                                                      │
│     {                                                                   │
│       "maturity_value": 164361.50,                                     │
│       "maturity_date": "2030-10-10",                                   │
│       "apy": 10.6508,                                                  │
│       "effective_rate": 10.2500,                                       │
│       "payout_freq": null,                                             │
│       "payout_amount": null,                                           │
│       "calc_id": 123,                                                  │
│       "result_id": 123                                                 │
│     }                                                                   │
│                                                                         │
│   ▶ 400 Invalid request parameters                                    │
│   ▶ 500 Server error                                                  │
│                                                                         │
└─────────────────────────────────────────────────────────────────────────┘
```

---

## Section 2: Reference Data (5 endpoints)

```
┌─────────────────────────────────────────────────────────────────────────┐
│ ▼ Reference Data                                                        │
│   Reference data endpoints for categories, currencies,                 │
│   compounding options, and rate cache management                       │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                         │
│ ▶ GET /api/fd/categories                                               │
│   Get all customer categories                                          │
│                                                                         │
│ ▶ GET /api/fd/currencies                                               │
│   Get supported currencies                                             │
│                                                                         │
│ ▶ GET /api/fd/compounding-options                                      │
│   Get compounding frequency options                                    │
│                                                                         │
│ ▶ POST /api/fd/rate-cache/refresh                                      │
│   Refresh rate cache                                                   │
│                                                                         │
│ ▶ GET /api/fd/rate-cache/{productCode}                                 │
│   Get cached base rate                                                 │
│                                                                         │
└─────────────────────────────────────────────────────────────────────────┘
```

### When You Expand: GET /api/fd/categories

```
┌─────────────────────────────────────────────────────────────────────────┐
│ ▼ GET /api/fd/categories                                               │
│   Get all customer categories                                          │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                         │
│   Retrieve all available customer categories and their benefit rates.  │
│                                                                         │
│   Categories include:                                                   │
│   • SENIOR: Senior Citizen - Additional 0.75% interest                │
│   • JR: Junior Citizen - Additional 0.50% interest                    │
│   • GOLD: Gold Customer - Additional 0.25% interest                   │
│   • SILVER: Silver Customer - Additional 0.15% interest               │
│   • PLAT: Platinum Customer - Additional 0.35% interest               │
│   • EMP: Employee - Additional 1.00% interest                         │
│   • DY: Divyang - Additional 1.25% interest                           │
│                                                                         │
│   [Try it out]                                                         │
│                                                                         │
│   [Execute]                                                            │
│                                                                         │
│   ─────────────────────────────────────────────────────────────────────│
│                                                                         │
│   ▼ 200 List of categories retrieved successfully                      │
│     Example Value:                                                      │
│     [                                                                   │
│       {                                                                 │
│         "category_id": 1,                                              │
│         "category_name": "Senior Citizen",                             │
│         "additional_percentage": 0.75                                  │
│       },                                                                │
│       {                                                                 │
│         "category_id": 2,                                              │
│         "category_name": "Gold Customer",                              │
│         "additional_percentage": 0.25                                  │
│       }                                                                 │
│     ]                                                                   │
│                                                                         │
└─────────────────────────────────────────────────────────────────────────┘
```

---

## Section 3: Admin (2 endpoints)

```
┌─────────────────────────────────────────────────────────────────────────┐
│ ▼ Admin                                                                 │
│   Administrative endpoints for product rule synchronization            │
│   and category management                                              │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                         │
│ ▶ POST /api/admin/sync-product-rules/{productCode}                     │
│   Sync product rules from Product & Pricing API                       │
│                                                                         │
│ ▶ GET /api/admin/categories                                            │
│   Get all categories from database                                     │
│                                                                         │
└─────────────────────────────────────────────────────────────────────────┘
```

---

## Bottom Section: Schemas

```
┌─────────────────────────────────────────────────────────────────────────┐
│ Schemas                                                                 │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                         │
│ ▶ FDCalculationRequest                                                 │
│   Fixed Deposit calculation request containing all parameters          │
│                                                                         │
│ ▶ FDCalculationResponse                                                │
│   Fixed Deposit calculation result with maturity details               │
│                                                                         │
│ ▶ CategoryDTO                                                          │
│   Customer category information with benefit rates                     │
│                                                                         │
└─────────────────────────────────────────────────────────────────────────┘
```

### When You Expand: FDCalculationRequest Schema

```
┌─────────────────────────────────────────────────────────────────────────┐
│ ▼ FDCalculationRequest                                                 │
│   Fixed Deposit calculation request containing all parameters needed   │
│   for FD calculation                                                    │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                         │
│   {                                                                     │
│     currency_code: string                                              │
│       Currency code for the deposit                                    │
│       Example: "INR"                                                   │
│       Pattern: ^(INR|JPY|AED)$                                        │
│                                                                         │
│     principal_amount: number ($decimal) *                              │
│       Principal amount to be deposited                                 │
│       Example: 100000                                                  │
│       Minimum: 0.01                                                    │
│                                                                         │
│     tenure_value: integer ($int32) *                                   │
│       Tenure value (numeric part of the tenure period)                 │
│       Example: 5                                                       │
│       Minimum: 1                                                       │
│                                                                         │
│     tenure_unit: string *                                              │
│       Tenure unit (time period unit)                                   │
│       Example: "YEARS"                                                 │
│       Enum: [ DAYS, MONTHS, YEARS ]                                   │
│                                                                         │
│     interest_type: string *                                            │
│       Type of interest calculation                                     │
│       Example: "COMPOUND"                                              │
│       Enum: [ SIMPLE, COMPOUND ]                                      │
│                                                                         │
│     compounding_frequency: string                                      │
│       Frequency of compounding                                         │
│       Example: "QUARTERLY"                                             │
│       Enum: [ DAILY, MONTHLY, QUARTERLY, YEARLY ]                    │
│                                                                         │
│     category1_id: string                                               │
│       Primary customer category code                                   │
│       Example: "SENIOR"                                                │
│       Enum: [ SENIOR, JR, GOLD, SILVER, PLAT, EMP, DY, ... ]        │
│                                                                         │
│     category2_id: string                                               │
│       Secondary customer category code                                 │
│       Example: "GOLD"                                                  │
│                                                                         │
│     cumulative: boolean                                                │
│       FD type: true = Cumulative, false = Non-Cumulative              │
│       Example: true                                                    │
│                                                                         │
│     payout_freq: string                                                │
│       Payout frequency for non-cumulative FDs                          │
│       Example: "QUARTERLY"                                             │
│       Enum: [ MONTHLY, QUARTERLY, YEARLY ]                           │
│                                                                         │
│     product_code: string *                                             │
│       Product code for the FD scheme                                   │
│       Example: "FD001"                                                 │
│   }                                                                     │
│                                                                         │
│   * = required                                                          │
│                                                                         │
└─────────────────────────────────────────────────────────────────────────┘
```

---

## Interactive Features You'll See

### 1. Try It Out Button
```
┌──────────────────┐
│  [Try it out]    │  ← Click this to enable editing
└──────────────────┘

After clicking:
┌──────────────────┐
│  [Cancel]        │  ← Click to disable editing
└──────────────────┘
```

### 2. Execute Button
```
┌──────────────────┐
│  [Execute]       │  ← Click to send the request
└──────────────────┘
```

### 3. Response Section (After Execute)
```
┌─────────────────────────────────────────────────────────────────────────┐
│ Server response                                                         │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                         │
│ Code: 200                                                               │
│                                                                         │
│ Details:                                                                │
│                                                                         │
│ Response body:                                                          │
│ {                                                                       │
│   "maturity_value": 164361.50,                                         │
│   "maturity_date": "2030-10-10",                                       │
│   "apy": 10.6508,                                                      │
│   "effective_rate": 10.2500,                                           │
│   "payout_freq": null,                                                 │
│   "payout_amount": null,                                               │
│   "calc_id": 123,                                                      │
│   "result_id": 123                                                     │
│ }                                                                       │
│                                                                         │
│ Response headers:                                                       │
│ content-type: application/json                                          │
│                                                                         │
│ Curl:                                                                   │
│ curl -X 'POST' \                                                       │
│   'http://localhost:8081/api/fd/calculate' \                          │
│   -H 'accept: application/json' \                                      │
│   -H 'Content-Type: application/json' \                                │
│   -d '{ "principal_amount": 100000, ... }'                            │
│                                                                         │
│ Request URL:                                                            │
│ http://localhost:8081/api/fd/calculate                                 │
│                                                                         │
└─────────────────────────────────────────────────────────────────────────┘
```

### 4. Example Dropdown
```
┌────────────────────────────────────────────────────┐
│ [Example Value ▼] [Schema ▼]                      │
└────────────────────────────────────────────────────┘

When clicked:
┌────────────────────────────────────────────────────┐
│ ✓ Cumulative FD - Senior Citizen                  │
│   Non-Cumulative FD - Gold Customer               │
│   Employee FD - Monthly Payout                    │
└────────────────────────────────────────────────────┘
```

### 5. Search/Filter
```
┌────────────────────────────────────────────────────┐
│ [Filter by tags _______________]                  │
└────────────────────────────────────────────────────┘

Type "calculate" to filter:
Shows only endpoints with "calculate" in name/description
```

---

## Color Coding (Visual Indicators)

```
HTTP Methods:

[POST]   - Blue/Green button
[GET]    - Blue button  
[PUT]    - Orange button
[DELETE] - Red button

Status Codes:

200 - Green (Success)
400 - Orange (Client Error)
404 - Orange (Not Found)
500 - Red (Server Error)
```

---

## Navigation Tips

### Expand/Collapse All
```
At the top of each section:
[ ▼ ]  ← Click to collapse
[ ▶ ]  ← Click to expand
```

### Anchor Links
```
Click any endpoint → URL updates with #anchor
Share URL with team → They see the same endpoint
```

### Keyboard Shortcuts
```
Ctrl/Cmd + F  → Browser search
Tab           → Navigate fields
Enter         → Submit when focused
```

---

## Mobile View

```
On mobile devices, Swagger UI adapts:

┌─────────────────────┐
│ NEXA Bank FD API    │
│ v1.0.0              │
├─────────────────────┤
│                     │
│ ▼ FD Calculator     │
│   ▶ POST calculate  │
│   ▶ GET calc/{id}   │
│   ▶ GET history     │
│                     │
│ ▶ Reference Data    │
│                     │
│ ▶ Admin             │
│                     │
└─────────────────────┘

(Optimized for touch)
```

---

## Print View

```
When you print (Ctrl/Cmd + P):

┌──────────────────────────────────────┐
│  NEXA Bank - FD Calculator API       │
│  Version: 1.0.0                      │
│                                      │
│  Endpoints                           │
│  ──────────                          │
│                                      │
│  FD Calculator                       │
│  • POST /api/fd/calculate           │
│  • GET /api/fd/calculations/{id}    │
│  • GET /api/fd/history              │
│                                      │
│  Reference Data                      │
│  • GET /api/fd/categories           │
│  • GET /api/fd/currencies           │
│  ...                                 │
└──────────────────────────────────────┘

(Clean, printer-friendly format)
```

---

## Export Options

### Download OpenAPI Spec
```
Top-right corner:
┌──────────────────┐
│ /v3/api-docs ▼   │  ← Click for menu
└──────────────────┘

Menu:
• View as JSON
• View as YAML
• Copy URL
• Download
```

---

## Summary - What Makes This Special

### ✅ Visual Excellence
- Clean, professional interface
- Color-coded HTTP methods
- Intuitive navigation
- Mobile-responsive

### ✅ Interactive Features
- Test APIs in browser
- Pre-filled examples
- Live request/response
- Copy curl commands

### ✅ Comprehensive
- All 10 endpoints
- 50+ examples
- Complete schemas
- Error responses

### ✅ User-Friendly
- Search/filter
- Expand/collapse
- Alphabetical sorting
- Clear descriptions

---

**This is what awaits you at:**
# http://localhost:8081/swagger-ui.html

**🎉 Professional, Interactive, Complete!**

---

**Last Updated:** October 10, 2025  
**Status:** Ready to Use
