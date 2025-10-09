#!/bin/bash

# FD Calculator - Product Rules Integration Test Script
# This script tests the integration between FD Calculator and Product & Pricing API

BASE_URL="http://localhost:8081"
PRICING_API_URL="http://localhost:8080"

echo "=========================================="
echo "FD Calculator - Product Rules Integration"
echo "=========================================="
echo ""

# Test 1: Check Product & Pricing API availability
echo "1. Testing Product & Pricing API connectivity..."
response=$(curl -s -w "\n%{http_code}" "${PRICING_API_URL}/api/products/FD001/rules")
http_code=$(echo "$response" | tail -n1)
if [ "$http_code" -eq 200 ]; then
    echo "   ✅ Product & Pricing API is available"
else
    echo "   ❌ Product & Pricing API is not available (HTTP $http_code)"
    echo "   Please ensure the Product & Pricing API is running on port 8080"
    exit 1
fi
echo ""

# Test 2: Check FD Calculator API availability
echo "2. Testing FD Calculator API connectivity..."
response=$(curl -s -w "\n%{http_code}" "${BASE_URL}/api/reference/categories")
http_code=$(echo "$response" | tail -n1)
if [ "$http_code" -eq 200 ]; then
    echo "   ✅ FD Calculator API is available"
else
    echo "   ❌ FD Calculator API is not available (HTTP $http_code)"
    echo "   Please ensure the FD Calculator is running on port 8081"
    exit 1
fi
echo ""

# Test 3: View product rules from Product & Pricing API
echo "3. Fetching product rules for FD001..."
curl -s "${PRICING_API_URL}/api/products/FD001/rules" | jq '.content[] | {ruleCode, ruleName, ruleValue}'
echo ""

# Test 4: Sync product rules to FD Calculator
echo "4. Syncing product rules to FD Calculator..."
response=$(curl -s -X POST "${BASE_URL}/api/admin/sync-product-rules/FD001")
echo "   Response: $response"
echo ""

# Test 5: View synced categories
echo "5. Viewing synced categories..."
curl -s "${BASE_URL}/api/admin/categories" | jq '.'
echo ""

# Test 6: Test MIN rule
echo "6. Testing MIN001 rule (fetching minimum amount)..."
curl -s "${PRICING_API_URL}/api/products/FD001/rules/MIN001" | jq '{ruleCode, ruleName, ruleValue}'
echo ""

# Test 7: Test MAX rule
echo "7. Testing MAX001 rule (fetching maximum amount)..."
curl -s "${PRICING_API_URL}/api/products/FD001/rules/MAX001" | jq '{ruleCode, ruleName, ruleValue}'
echo ""

# Test 8: Test MAXINT rule
echo "8. Testing MAXINT001 rule (fetching maximum excess interest)..."
curl -s "${PRICING_API_URL}/api/products/FD001/rules/MAXINT001" | jq '{ruleCode, ruleName, ruleValue}'
echo ""

# Test 9: Calculate FD with valid amount
echo "9. Testing FD calculation with valid amount (50000)..."
response=$(curl -s -X POST "${BASE_URL}/api/fd-calculator/calculate" \
  -H "Content-Type: application/json" \
  -d '{
    "product_code": "FD001",
    "currency_code": "INR",
    "principal_amount": 50000,
    "tenure_value": 12,
    "tenure_unit": "MONTHS",
    "interest_type": "COMPOUND",
    "compounding_frequency": "QUARTERLY",
    "category1_id": 1,
    "category2_id": 2
  }')
echo "   Response: $response" | jq '.'
echo ""

# Test 10: Calculate FD with amount below minimum (should fail)
echo "10. Testing FD calculation with amount below minimum (5000)..."
response=$(curl -s -X POST "${BASE_URL}/api/fd-calculator/calculate" \
  -H "Content-Type: application/json" \
  -d '{
    "product_code": "FD001",
    "currency_code": "INR",
    "principal_amount": 5000,
    "tenure_value": 12,
    "tenure_unit": "MONTHS",
    "interest_type": "COMPOUND",
    "compounding_frequency": "QUARTERLY",
    "category1_id": 1
  }')
echo "   Response: $response"
echo ""

# Test 11: Calculate FD with amount above maximum (should fail)
echo "11. Testing FD calculation with amount above maximum (600000)..."
response=$(curl -s -X POST "${BASE_URL}/api/fd-calculator/calculate" \
  -H "Content-Type: application/json" \
  -d '{
    "product_code": "FD001",
    "currency_code": "INR",
    "principal_amount": 600000,
    "tenure_value": 12,
    "tenure_unit": "MONTHS",
    "interest_type": "COMPOUND",
    "compounding_frequency": "QUARTERLY",
    "category1_id": 1
  }')
echo "   Response: $response"
echo ""

echo "=========================================="
echo "Integration tests completed!"
echo "=========================================="
