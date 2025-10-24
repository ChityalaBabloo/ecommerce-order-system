# API Testing Guide

## Complete Testing Scenarios for Order Processing System

This guide provides comprehensive testing scenarios to validate all features of the E-commerce Order Processing System.

---

## Prerequisites

1. **Start the application**:
   ```bash
   cd /tmp/ecommerce-order-system
   mvn spring-boot:run
   ```

2. **Verify it's running**:
   - Server should be running on: `http://localhost:8080`
   - Check logs for: "Started OrderProcessingApplication"

---

## Test Scenario 1: Complete Order Lifecycle

### Step 1: Create a New Order
```bash
curl -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -d '{
    "customerName": "Test Customer",
    "customerEmail": "test@example.com",
    "items": [
      {
        "productName": "Laptop",
        "quantity": 1,
        "price": 1200.00
      },
      {
        "productName": "Mouse",
        "quantity": 2,
        "price": 25.00
      }
    ]
  }'
```

**Expected Response** (Status: 201 Created):
```json
{
  "id": 1,
  "customerName": "Test Customer",
  "customerEmail": "test@example.com",
  "status": "PENDING",
  "items": [...],
  "totalAmount": 1250.00,
  "createdAt": "...",
  "updatedAt": "..."
}
```

**Validation**:
- ✅ Status should be 201 Created
- ✅ Order ID should be generated
- ✅ Status should be PENDING
- ✅ Total amount = 1200 + (2 × 25) = 1250.00

---

### Step 2: Retrieve the Order
```bash
curl http://localhost:8080/api/orders/1
```

**Expected Response** (Status: 200 OK):
```json
{
  "id": 1,
  "customerName": "Test Customer",
  "status": "PENDING",
  ...
}
```

**Validation**:
- ✅ Status should be 200 OK
- ✅ Order details match created order

---

### Step 3: Update Status to PROCESSING
```bash
curl -X PUT "http://localhost:8080/api/orders/1/status?status=PROCESSING"
```

**Expected Response** (Status: 200 OK):
```json
{
  "id": 1,
  "status": "PROCESSING",
  ...
}
```

**Validation**:
- ✅ Status changed to PROCESSING
- ✅ updatedAt timestamp changed

---

### Step 4: Update Status to SHIPPED
```bash
curl -X PUT "http://localhost:8080/api/orders/1/status?status=SHIPPED"
```

**Expected Response** (Status: 200 OK):
```json
{
  "id": 1,
  "status": "SHIPPED",
  ...
}
```

---

### Step 5: Update Status to DELIVERED
```bash
curl -X PUT "http://localhost:8080/api/orders/1/status?status=DELIVERED"
```

**Expected Response** (Status: 200 OK):
```json
{
  "id": 1,
  "status": "DELIVERED",
  ...
}
```

**Validation**:
- ✅ Order completed successfully
- ✅ Status is terminal (cannot be changed further)

---

## Test Scenario 2: Order Cancellation

### Step 1: Create Another Order
```bash
curl -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -d '{
    "customerName": "Cancel Test",
    "customerEmail": "cancel@example.com",
    "items": [
      {
        "productName": "Phone",
        "quantity": 1,
        "price": 800.00
      }
    ]
  }'
```

**Note the order ID** (e.g., ID = 2)

---

### Step 2: Cancel While PENDING
```bash
curl -X POST http://localhost:8080/api/orders/2/cancel
```

**Expected Response** (Status: 200 OK):
```json
{
  "id": 2,
  "status": "CANCELLED",
  ...
}
```

**Validation**:
- ✅ Order successfully cancelled
- ✅ Status changed to CANCELLED

---

### Step 3: Try to Cancel Again (Should Fail)
```bash
curl -X POST http://localhost:8080/api/orders/2/cancel
```

**Expected Response** (Status: 400 Bad Request):
```json
{
  "timestamp": "...",
  "status": 400,
  "error": "Bad Request",
  "message": "Order cannot be cancelled. Current status: CANCELLED",
  "path": "/api/orders/2/cancel"
}
```

**Validation**:
- ✅ Cannot cancel already cancelled order
- ✅ Proper error message returned

---

## Test Scenario 3: Invalid Cancellation

### Step 1: Create Order and Move to PROCESSING
```bash
# Create order
curl -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -d '{
    "customerName": "No Cancel",
    "customerEmail": "nocancel@example.com",
    "items": [{"productName": "Item", "quantity": 1, "price": 100.00}]
  }'

# Update to PROCESSING (note: replace {id} with actual ID)
curl -X PUT "http://localhost:8080/api/orders/{id}/status?status=PROCESSING"
```

---

### Step 2: Try to Cancel (Should Fail)
```bash
curl -X POST http://localhost:8080/api/orders/{id}/cancel
```

**Expected Response** (Status: 400 Bad Request):
```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "Order cannot be cancelled. Current status: PROCESSING"
}
```

**Validation**:
- ✅ Cannot cancel non-PENDING orders
- ✅ Clear error message

---

## Test Scenario 4: List and Filter Orders

### Step 1: Get All Orders
```bash
curl http://localhost:8080/api/orders
```

**Expected Response** (Status: 200 OK):
```json
[
  {
    "id": 1,
    "status": "DELIVERED",
    ...
  },
  {
    "id": 2,
    "status": "CANCELLED",
    ...
  },
  ...
]
```

---

### Step 2: Filter by PENDING Status
```bash
curl "http://localhost:8080/api/orders?status=PENDING"
```

**Expected Response**: Array of only PENDING orders

---

### Step 3: Filter by PROCESSING Status
```bash
curl "http://localhost:8080/api/orders?status=PROCESSING"
```

---

### Step 4: Filter by SHIPPED Status
```bash
curl "http://localhost:8080/api/orders?status=SHIPPED"
```

---

### Step 5: Filter by DELIVERED Status
```bash
curl "http://localhost:8080/api/orders?status=DELIVERED"
```

**Validation**:
- ✅ Filtering works correctly
- ✅ Only orders with specified status returned

---

## Test Scenario 5: Background Job (Auto-Processing)

### Step 1: Create Multiple PENDING Orders
```bash
# Create Order 1
curl -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -d '{
    "customerName": "Auto Test 1",
    "customerEmail": "auto1@example.com",
    "items": [{"productName": "Item A", "quantity": 1, "price": 50.00}]
  }'

# Create Order 2
curl -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -d '{
    "customerName": "Auto Test 2",
    "customerEmail": "auto2@example.com",
    "items": [{"productName": "Item B", "quantity": 1, "price": 75.00}]
  }'
```

---

### Step 2: Check Current Status
```bash
curl "http://localhost:8080/api/orders?status=PENDING"
```

**Note**: Should return the newly created PENDING orders

---

### Step 3: Wait for Scheduler (5 Minutes)
⏰ Wait for 5 minutes for the background job to run

**Alternative**: To test immediately, you can restart the application with a shorter interval:
- Modify `OrderScheduler.java`: Change `fixedRate = 300000` to `fixedRate = 10000` (10 seconds)
- Rebuild and run

---

### Step 4: Check Status Again
```bash
curl "http://localhost:8080/api/orders?status=PROCESSING"
```

**Expected**: Previously PENDING orders should now be PROCESSING

**Validation**:
- ✅ Background job executed
- ✅ PENDING orders moved to PROCESSING
- ✅ Check logs for: "Processed X pending orders"

---

## Test Scenario 6: Validation Errors

### Test 6.1: Empty Customer Name
```bash
curl -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -d '{
    "customerName": "",
    "customerEmail": "test@example.com",
    "items": [{"productName": "Item", "quantity": 1, "price": 10.00}]
  }'
```

**Expected Response** (Status: 400 Bad Request):
```json
{
  "status": 400,
  "error": "Validation Failed",
  "message": "Input validation failed",
  "details": [
    "customerName: Customer name is required"
  ]
}
```

---

### Test 6.2: Invalid Email
```bash
curl -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -d '{
    "customerName": "Test",
    "customerEmail": "invalid-email",
    "items": [{"productName": "Item", "quantity": 1, "price": 10.00}]
  }'
```

**Expected Response** (Status: 400 Bad Request):
```json
{
  "status": 400,
  "error": "Validation Failed",
  "details": [
    "customerEmail: Invalid email format"
  ]
}
```

---

### Test 6.3: Empty Items List
```bash
curl -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -d '{
    "customerName": "Test",
    "customerEmail": "test@example.com",
    "items": []
  }'
```

**Expected Response** (Status: 400 Bad Request):
```json
{
  "details": [
    "items: Order must contain at least one item"
  ]
}
```

---

### Test 6.4: Invalid Quantity (Zero or Negative)
```bash
curl -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -d '{
    "customerName": "Test",
    "customerEmail": "test@example.com",
    "items": [{"productName": "Item", "quantity": 0, "price": 10.00}]
  }'
```

**Expected Response** (Status: 400 Bad Request):
```json
{
  "details": [
    "items[0].quantity: Quantity must be at least 1"
  ]
}
```

---

### Test 6.5: Negative Price
```bash
curl -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -d '{
    "customerName": "Test",
    "customerEmail": "test@example.com",
    "items": [{"productName": "Item", "quantity": 1, "price": -10.00}]
  }'
```

**Expected Response** (Status: 400 Bad Request):
```json
{
  "details": [
    "items[0].price: Price must be non-negative"
  ]
}
```

---

## Test Scenario 7: Error Handling

### Test 7.1: Order Not Found
```bash
curl http://localhost:8080/api/orders/99999
```

**Expected Response** (Status: 404 Not Found):
```json
{
  "timestamp": "...",
  "status": 404,
  "error": "Not Found",
  "message": "Order not found with id: 99999",
  "path": "/api/orders/99999"
}
```

---

### Test 7.2: Invalid Status Transition
```bash
# Create order (will be PENDING)
curl -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -d '{
    "customerName": "Invalid Transition",
    "customerEmail": "invalid@example.com",
    "items": [{"productName": "Item", "quantity": 1, "price": 10.00}]
  }'

# Try to jump to SHIPPED (invalid from PENDING)
curl -X PUT "http://localhost:8080/api/orders/{id}/status?status=SHIPPED"
```

**Expected Response** (Status: 400 Bad Request):
```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "PENDING orders can only move to PROCESSING or be CANCELLED"
}
```

---

## Test Scenario 8: Complex Order with Multiple Items

```bash
curl -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -d '{
    "customerName": "Complex Order User",
    "customerEmail": "complex@example.com",
    "items": [
      {
        "productName": "Laptop",
        "quantity": 2,
        "price": 1500.00
      },
      {
        "productName": "Monitor",
        "quantity": 2,
        "price": 300.00
      },
      {
        "productName": "Keyboard",
        "quantity": 2,
        "price": 100.00
      },
      {
        "productName": "Mouse",
        "quantity": 2,
        "price": 50.00
      }
    ]
  }'
```

**Expected Response**:
- Total = (2 × 1500) + (2 × 300) + (2 × 100) + (2 × 50) = 4000.00
- All 4 items with correct subtotals

**Validation**:
- ✅ Multiple items handled correctly
- ✅ Total calculated accurately
- ✅ Each item has correct subtotal

---

## Testing Checklist

### Core Features
- ✅ Create order with single item
- ✅ Create order with multiple items
- ✅ Retrieve order by ID
- ✅ List all orders
- ✅ Filter orders by status
- ✅ Update order status (valid transitions)
- ✅ Cancel PENDING order
- ✅ Background job auto-processes orders

### Validation
- ✅ Empty customer name rejected
- ✅ Invalid email rejected
- ✅ Empty items list rejected
- ✅ Invalid quantity rejected
- ✅ Negative price rejected

### Error Handling
- ✅ Order not found (404)
- ✅ Invalid status transition (400)
- ✅ Cannot cancel non-PENDING order (400)
- ✅ Cannot cancel already cancelled order (400)
- ✅ Cannot update terminal status orders (400)

### Edge Cases
- ✅ Multiple items calculation
- ✅ Decimal price handling
- ✅ Large quantities
- ✅ Concurrent order creation

---

## Automated Testing

### Run Unit Tests
```bash
mvn test -Dtest=OrderServiceTest
```

### Run Controller Tests
```bash
mvn test -Dtest=OrderControllerTest
```

### Run Integration Tests
```bash
mvn test -Dtest=OrderProcessingIntegrationTest
```

### Run All Tests
```bash
mvn test
```

---

## Database Inspection

### Access H2 Console
1. Open browser: http://localhost:8080/h2-console
2. JDBC URL: `jdbc:h2:mem:orderdb`
3. Username: `sa`
4. Password: (empty)
5. Click "Connect"

### SQL Queries to Verify Data

```sql
-- View all orders
SELECT * FROM orders;

-- View all order items
SELECT * FROM order_items;

-- Count orders by status
SELECT status, COUNT(*) as count 
FROM orders 
GROUP BY status;

-- View orders with items (JOIN)
SELECT o.id, o.customer_name, o.status, oi.product_name, oi.quantity, oi.price
FROM orders o
JOIN order_items oi ON o.id = oi.order_id
ORDER BY o.id;

-- Find PENDING orders
SELECT * FROM orders WHERE status = 'PENDING';
```

---

## Performance Testing

### Create Multiple Orders Quickly
```bash
for i in {1..10}
do
  curl -X POST http://localhost:8080/api/orders \
    -H "Content-Type: application/json" \
    -d "{
      \"customerName\": \"Perf Test User $i\",
      \"customerEmail\": \"perf$i@example.com\",
      \"items\": [{\"productName\": \"Item $i\", \"quantity\": 1, \"price\": 10.00}]
    }"
done
```

### Verify All Created
```bash
curl http://localhost:8080/api/orders | grep -o '"id"' | wc -l
```

---

## Success Criteria

✅ All test scenarios pass  
✅ All validation errors caught  
✅ All error responses properly formatted  
✅ Background job processes orders correctly  
✅ Status transitions follow business rules  
✅ Database integrity maintained  
✅ No exceptions in logs (except handled ones)  
✅ All unit tests pass  
✅ All integration tests pass  

---

## Common Issues & Solutions

### Issue 1: Port 8080 Already in Use
```bash
# Find and kill process
lsof -i :8080
kill -9 <PID>

# Or change port in application.properties
server.port=8081
```

### Issue 2: Background Job Not Running
- Check logs for: "Running scheduled task"
- Verify `@EnableScheduling` in main application class
- Check scheduler configuration

### Issue 3: Database Reset
- H2 is in-memory, restarting app resets database
- For persistent testing, create orders after each restart

---

## Conclusion

This comprehensive testing guide covers all aspects of the Order Processing System. Use these tests to verify functionality, validate error handling, and ensure the system meets all requirements.

Happy Testing! 🎉
