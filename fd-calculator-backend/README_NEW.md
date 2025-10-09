# Fixed Deposit Calculator - Backend

This is the backend application for the FD Calculator with integrated Product & Pricing API support.

## 🚀 Quick Start

### Prerequisites
- Java 17+
- Maven
- MySQL 8.0+
- Product & Pricing API running on port 8080

### Running the Application

**Option 1: Mock Mode (for testing without external API)**
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=mock
```

**Option 2: Production Mode (with Product & Pricing API)**
```bash
# 1. Configure MySQL and create database 'fd_calculator'
# 2. Ensure Product & Pricing API is running on port 8080
# 3. Run the application
mvn spring-boot:run -Dspring-boot.run.profiles=prod
```

The application will start on port 8081.

### Initial Setup

**Sync Product Rules (Required for Production)**
```bash
curl -X POST http://localhost:8081/api/admin/sync-product-rules/FD001
```

**Test FD Calculation**
```bash
curl -X POST http://localhost:8081/api/fd-calculator/calculate \
  -H "Content-Type: application/json" \
  -d '{
    "product_code": "FD001",
    "principal_amount": 50000,
    "tenure_value": 12,
    "tenure_unit": "MONTHS",
    "interest_type": "COMPOUND",
    "compounding_frequency": "QUARTERLY",
    "category1_id": 1,
    "category2_id": 2
  }'
```

## 📚 Documentation

- **[Implementation Summary](IMPLEMENTATION_SUMMARY.md)** - Complete overview of what was implemented
- **[Product Rules Integration](PRODUCT_RULES_INTEGRATION.md)** - Detailed integration guide
- **[Architecture Diagram](ARCHITECTURE.md)** - Visual system architecture and data flows

## 🎯 Key Features

### ✅ Dynamic Product Rules
- Fetches MIN/MAX amounts from Product & Pricing API
- Configurable maximum excess interest per product
- Real-time rule validation

### ✅ Rule-Based Categories
- Automatic sync of benefit rules (JR, SR, DY)
- Pattern-based rule code matching
- Centralized rule management

### ✅ Smart Validation
- Amount validation against product constraints
- Dynamic limit enforcement
- Graceful fallback handling

## 🔧 API Endpoints

### Admin Endpoints
```
POST /api/admin/sync-product-rules/{productCode}  # Sync product rules to categories
GET  /api/admin/categories                         # View all synced categories
```

### Calculator Endpoints
```
POST /api/fd-calculator/calculate                  # Calculate FD maturity
GET  /api/fd-calculator/calculation/{calcId}       # Get calculation by ID
```

### Reference Data Endpoints
```
GET  /api/reference/categories                     # Get all categories
```

## 🧪 Testing

Run the integration test script:
```bash
./test-integration.sh
```

This script will:
1. Test API connectivity
2. Fetch and display product rules
3. Sync rules to categories
4. Test FD calculations with various scenarios
5. Validate error handling

## 📋 Product Rules Convention

For product code `FD001` (last 3 digits = `001`):

| Rule Code | Purpose | Type |
|-----------|---------|------|
| `MIN001` | Minimum deposit amount | Constraint |
| `MAX001` | Maximum deposit amount | Constraint |
| `MAXINT001` | Maximum excess interest | Constraint |
| `JR001` | Junior benefit (under 18) | Category |
| `SR001` | Senior citizen benefit | Category |
| `DY001` | Digi Youth benefit | Category |

## 🔌 Integration Architecture

```
FD Calculator (8081) ←→ Product & Pricing API (8080)
       ↓
   MySQL Database
```

**What's Synced:**
- Benefit categories (JR, SR, DY) → Stored in Category table
- Constraint rules (MIN, MAX, MAXINT) → Fetched on-demand for validation

## ⚙️ Configuration

**application.yml:**
```yaml
pricing:
  api:
    url: http://localhost:8080

server:
  port: 8081

spring:
  datasource:
    url: jdbc:mysql://localhost:3306/fd_calculator
    username: root
    password: your_password
```

## 📝 Example Calculation Flow

1. **Request:** Calculate FD with ₹50,000 for 12 months
2. **Validation:** Check 50,000 is between MIN001 (10,000) and MAX001 (500,000) ✓
3. **Base Rate:** Fetch base rate for FD001
4. **Benefits:** Apply Junior (0.5%) + Senior (0.75%) = 1.25%
5. **Cap:** Ensure 1.25% ≤ MAXINT001 (2%) ✓
6. **Calculate:** Compute maturity with effective rate
7. **Response:** Return maturity value, date, APY

## 🛠️ Built With

- **Spring Boot 3.2.3** - Application framework
- **Spring Cloud OpenFeign** - REST client for Product API
- **MySQL** - Database
- **Lombok** - Reduce boilerplate code
- **Maven** - Dependency management

## 📊 Sample Product Rules (FD001)

```json
{
  "MIN001": "10000",      // Minimum deposit
  "MAX001": "500000",     // Maximum deposit
  "MAXINT001": "2",       // Max excess interest (%)
  "JR001": "0.5",         // Junior benefit (%)
  "SR001": "0.75",        // Senior citizen benefit (%)
  "DY001": "0.25"         // Digi Youth benefit (%)
}
```

## 🐛 Troubleshooting

### Issue: Cannot connect to Product & Pricing API
**Solution:** Ensure the Product & Pricing API is running on port 8080

### Issue: Amount validation fails
**Solution:** Check that product rules are synced:
```bash
curl -X POST http://localhost:8081/api/admin/sync-product-rules/FD001
```

### Issue: Categories not found
**Solution:** Verify categories exist in database after sync:
```bash
curl http://localhost:8081/api/admin/categories
```

## 📄 License

This project is part of the NEXA-BANK-FDC system.
