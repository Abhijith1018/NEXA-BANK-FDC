# Quick Start - Swagger Documentation

## 🚀 Start Using Swagger in 3 Steps

### Step 1: Start the Application
```bash
cd /Users/Jaiwant/repos/NEXA-BANK-FDC/fd-calculator-backend
mvn spring-boot:run
```

### Step 2: Open Swagger UI
Open your browser and go to:
```
http://localhost:8081/swagger-ui.html
```

### Step 3: Try Your First API Call

1. Click on **"FD Calculator"** section
2. Click on **"POST /api/fd/calculate"**
3. Click **"Try it out"** button
4. Click **"Execute"** button (uses pre-filled example)
5. See the result in the **Response body** section

**That's it! You're testing your API!**

---

## 📌 Quick Links

| Resource | URL |
|----------|-----|
| **Swagger UI** | http://localhost:8081/swagger-ui.html |
| **API Docs (JSON)** | http://localhost:8081/v3/api-docs |
| **API Docs (YAML)** | http://localhost:8081/v3/api-docs.yaml |

---

## 🎯 Common Tasks

### Calculate Cumulative FD
```
1. Go to: POST /api/fd/calculate
2. Click "Try it out"
3. Use the "Cumulative FD - Senior Citizen" example
4. Click "Execute"
5. See maturity_value in response
```

### Calculate Non-Cumulative FD
```
1. Go to: POST /api/fd/calculate
2. Click "Try it out"
3. Use the "Non-Cumulative FD - Gold Customer" example
4. Click "Execute"
5. See payout_amount in response
```

### Get Customer Categories
```
1. Go to: GET /api/fd/categories
2. Click "Try it out"
3. Click "Execute"
4. See list of categories with benefit rates
```

### Refresh Rate Cache
```
1. Go to: POST /api/fd/rate-cache/refresh
2. Click "Try it out"
3. Enter productCode: "FD001"
4. Click "Execute"
5. See "Refreshed FD001" message
```

---

## 📖 What You'll See

### Main Sections (Tags)
1. **FD Calculator** - Calculate FD returns
2. **Reference Data** - Categories, currencies, compounding options
3. **Admin** - Product rule sync, category management

### For Each Endpoint
- 📝 **Description** - What it does
- 📥 **Request** - What to send
- 📤 **Response** - What you get back
- 💡 **Examples** - Ready-to-use samples
- ⚠️ **Errors** - Possible error responses

---

## 🎓 Swagger UI Features

### 🔍 Search
Type in the search bar to filter endpoints

### 📋 Try It Out
Execute API calls directly from browser

### 💾 Download
Download OpenAPI spec for offline use

### 📤 Export
Copy as curl command or generate client code

### 🔄 Reset
Click "Clear" to reset form fields

---

## 🏷️ Key Terms

| Term | Meaning | Example |
|------|---------|---------|
| **Endpoint** | An API operation/URL | POST /api/fd/calculate |
| **Tag** | Group of endpoints | "FD Calculator" |
| **Schema** | Data structure | FDCalculationRequest |
| **Operation** | API action | "Calculate Fixed Deposit" |
| **Try It Out** | Test the API | Click to enable editing |
| **Execute** | Send request | Click to call API |

---

## 💡 Pro Tips

### Tip 1: Use Examples
All endpoints have pre-filled examples. Just click "Execute"!

### Tip 2: Explore Schemas
Click on any schema (e.g., FDCalculationRequest) to see all fields

### Tip 3: Copy as Curl
After executing, scroll down to see the curl command. Copy it for terminal use!

### Tip 4: Filter Operations
Use the filter box to quickly find specific endpoints

### Tip 5: Collapse/Expand
Click section headers to show/hide details

---

## 🐛 Troubleshooting

### Swagger UI Not Loading?
```bash
# Check if app is running
curl http://localhost:8081/actuator/health

# Or check if port is in use
lsof -i :8081
```

### "Failed to Fetch"?
```
Make sure the application is running on port 8081
```

### Can't Execute API?
```
1. Click "Try it out" first
2. Fill in required fields
3. Then click "Execute"
```

---

## 📚 Documentation Files

| File | Purpose |
|------|---------|
| **SWAGGER_SETUP.md** | Complete Swagger setup guide |
| **API_DOCUMENTATION.md** | Detailed API reference |
| **NON_CUMULATIVE_APY_FIX.md** | APY calculation details |
| **Quick_Start_Swagger.md** | This file - Quick reference |

---

## ✅ What's Documented

### All Controllers
✅ FDCalculatorController (3 endpoints)  
✅ ReferenceDataController (5 endpoints)  
✅ AdminController (2 endpoints)  

### All DTOs
✅ FDCalculationRequest (11 fields)  
✅ FDCalculationResponse (8 fields)  
✅ CategoryDTO (3 fields)  

### All Features
✅ Cumulative FD calculations  
✅ Non-cumulative FD with payouts  
✅ Category benefits (10 categories)  
✅ Dynamic interest rates  
✅ APY calculation  
✅ Rate caching  

---

## 🎉 You're Ready!

Everything is set up and ready to use. Just:

1. ✅ Start your application
2. ✅ Open http://localhost:8081/swagger-ui.html
3. ✅ Start testing!

**Questions?** Check the full documentation in:
- SWAGGER_SETUP.md
- API_DOCUMENTATION.md

---

**Happy Testing! 🚀**

---

**Last Updated:** October 10, 2025  
**Version:** 1.0.0
