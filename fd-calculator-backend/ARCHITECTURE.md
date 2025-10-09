# Product Rules Integration Architecture

## System Architecture

```
┌─────────────────────────────────────────────────────────────────────┐
│                         Client Application                          │
└──────────────────────┬──────────────────────────────────────────────┘
                       │
                       ▼
┌─────────────────────────────────────────────────────────────────────┐
│                    FD Calculator Service (Port 8081)                 │
├─────────────────────────────────────────────────────────────────────┤
│                                                                       │
│  ┌────────────────┐  ┌──────────────────┐  ┌────────────────────┐  │
│  │ FD Calculator  │  │  Admin           │  │  Reference Data    │  │
│  │ Controller     │  │  Controller      │  │  Controller        │  │
│  └────────┬───────┘  └────────┬─────────┘  └────────────────────┘  │
│           │                    │                                     │
│  ┌────────▼──────────────────┐│┌─────────────────────────────────┐ │
│  │ FDCalculatorServiceImpl    ││ ProductRuleSyncServiceImpl      │ │
│  │  - calculate()             ││  - syncProductRulesToCategories() │
│  │  - validates amount        ││  - getAllCategories()            │ │
│  │  - applies categories      │└─────────────┬───────────────────┘ │
│  │  - uses dynamic max excess │              │                     │
│  └────────┬───────────────────┘              │                     │
│           │                    ┌──────────────▼──────────────────┐ │
│           │                    │ ProductRuleValidationServiceImpl │ │
│           │                    │  - validateAmount()              │ │
│           │                    │  - getMinimumAmount()           │ │
│           └────────────────────┤  - getMaximumAmount()           │ │
│                                │  - getMaximumExcessInterest()   │ │
│                                └──────────────┬──────────────────┘ │
│                                               │                     │
│                                    ┌──────────▼──────────────┐     │
│                                    │   PricingApiClient      │     │
│                                    │  (Feign Client)         │     │
│                                    │  - getRules()           │     │
│                                    │  - getRuleByCode()      │     │
│                                    └──────────┬──────────────┘     │
│                                               │                     │
│  ┌────────────────────────────────────────────┴──────────────────┐ │
│  │                      Database (MySQL)                          │ │
│  │  ┌──────────────┐  ┌────────────────┐  ┌──────────────────┐  │ │
│  │  │  Category    │  │  FD Input      │  │  FD Result       │  │ │
│  │  │  - id        │  │  - principal   │  │  - maturity_val  │  │ │
│  │  │  - name      │  │  - tenure      │  │  - maturity_date │  │ │
│  │  │  - add_pct   │  │  - categories  │  │  - effective_rate│  │ │
│  │  └──────────────┘  └────────────────┘  └──────────────────┘  │ │
│  └───────────────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────┬───────────────────────────┘
                                          │ HTTP/REST
                                          ▼
┌─────────────────────────────────────────────────────────────────────┐
│              Product & Pricing API (Port 8080)                       │
├─────────────────────────────────────────────────────────────────────┤
│                                                                       │
│  Endpoints:                                                          │
│  • GET  /api/products/{productCode}/rules                           │
│  • GET  /api/products/{productCode}/rules/{ruleCode}                │
│                                                                       │
│  Sample Rules for FD001:                                            │
│  ┌────────────────────────────────────────────────────────┐        │
│  │ Rule Code │ Rule Name              │ Rule Value │      │        │
│  ├───────────┼────────────────────────┼────────────┤      │        │
│  │ MIN001    │ Minimum for FD001      │ 10000      │      │        │
│  │ MAX001    │ Maximum for FD001      │ 500000     │      │        │
│  │ MAXINT001 │ Max excess interest    │ 2          │      │        │
│  │ JR001     │ Junior Benefit         │ 0.5        │ ✓    │        │
│  │ SR001     │ Senior Citizen Benefit │ 0.75       │ ✓    │        │
│  │ DY001     │ Digi Youth Benefit     │ 0.25       │ ✓    │        │
│  └────────────────────────────────────────────────────────┘        │
│                                          ✓ = Synced as Category     │
└─────────────────────────────────────────────────────────────────────┘
```

## Data Flow

### 1. Sync Product Rules
```
Admin → POST /api/admin/sync-product-rules/FD001
   │
   ├─→ ProductRuleSyncServiceImpl
   │      │
   │      ├─→ PricingApiClient.getRules("FD001")
   │      │      │
   │      │      └─→ Product & Pricing API: GET /api/products/FD001/rules
   │      │             │
   │      │             └─→ Returns: [MIN001, MAX001, MAXINT001, JR001, SR001, DY001]
   │      │
   │      ├─→ Filter benefit rules (JR001, SR001, DY001)
   │      │
   │      └─→ Save/Update in Category table
   │             ├─→ Junior Benefit (Under 18) → 0.5%
   │             ├─→ Senior Citizen Benefit → 0.75%
   │             └─→ Digi Youth Benefit → 0.25%
   │
   └─→ Response: {"status": "success"}
```

### 2. FD Calculation
```
Client → POST /api/fd-calculator/calculate
   │      {
   │        "product_code": "FD001",
   │        "principal_amount": 50000,
   │        "category1_id": 1,  // Junior
   │        "category2_id": 2   // Senior
   │      }
   │
   ├─→ FDCalculatorServiceImpl.calculate()
   │      │
   │      ├─→ ProductRuleValidationService.validateAmount("FD001", 50000)
   │      │      │
   │      │      ├─→ PricingApiClient.getRuleByCode("FD001", "MIN001")
   │      │      │      └─→ Product API: GET /api/products/FD001/rules/MIN001
   │      │      │             └─→ Returns: {"ruleValue": "10000"}
   │      │      │
   │      │      ├─→ PricingApiClient.getRuleByCode("FD001", "MAX001")
   │      │      │      └─→ Product API: GET /api/products/FD001/rules/MAX001
   │      │      │             └─→ Returns: {"ruleValue": "500000"}
   │      │      │
   │      │      └─→ Validate: 10000 ≤ 50000 ≤ 500000 ✓
   │      │
   │      ├─→ Fetch base rate for FD001
   │      │
   │      ├─→ Apply category benefits:
   │      │      Category 1 (Junior): +0.5%
   │      │      Category 2 (Senior): +0.75%
   │      │      Total extra: 1.25%
   │      │
   │      ├─→ ProductRuleValidationService.getMaximumExcessInterest("FD001")
   │      │      └─→ PricingApiClient.getRuleByCode("FD001", "MAXINT001")
   │      │             └─→ Product API: GET /api/products/FD001/rules/MAXINT001
   │      │                    └─→ Returns: {"ruleValue": "2"}
   │      │
   │      ├─→ Cap excess: min(1.25%, 2%) = 1.25% ✓
   │      │
   │      ├─→ Calculate maturity with effective rate
   │      │
   │      └─→ Save calculation to database
   │
   └─→ Response: {
         "maturity_value": 52345.67,
         "effective_rate": 8.25,
         "apy": 8.45
       }
```

## Rule Code Pattern Matching

```
Product Code: FD001
              └─┬─┘
                │
            Suffix: "001"
                │
                ├─→ MIN  + "001" = MIN001  (Minimum Amount)
                ├─→ MAX  + "001" = MAX001  (Maximum Amount)
                ├─→ MAXINT + "001" = MAXINT001 (Max Excess Interest)
                ├─→ JR   + "001" = JR001   (Junior Benefit) ✓ Category
                ├─→ SR   + "001" = SR001   (Senior Benefit) ✓ Category
                └─→ DY   + "001" = DY001   (Digi Youth)     ✓ Category
```

## Component Responsibilities

| Component | Responsibility | Storage |
|-----------|---------------|---------|
| **Product & Pricing API** | Master source of product rules | Product DB |
| **ProductRuleSyncService** | Sync benefit rules to categories | Category Table |
| **ProductRuleValidationService** | Validate constraints (MIN, MAX, MAXINT) | API calls |
| **FDCalculatorService** | Apply rules and calculate FD | FD Tables |
| **Category Repository** | Store synced benefit categories | MySQL |

## Rule Categories

### Constraint Rules (Not Stored as Categories)
- **MIN**: Minimum deposit amount
- **MAX**: Maximum deposit amount
- **MAXINT**: Maximum excess interest percentage

**Usage**: Direct API calls during validation

### Benefit Rules (Stored as Categories)
- **JR**: Junior benefit (under 18)
- **SR**: Senior citizen benefit
- **DY**: Digi Youth benefit

**Usage**: Synced to Category table, applied during calculation
