# System Architecture Diagrams

## 1. Application Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                      Client Applications                     │
│  (Postman, cURL, Browser, Mobile App, Web Frontend, etc.)  │
└──────────────────────────┬──────────────────────────────────┘
                           │ HTTP/REST
                           ▼
┌─────────────────────────────────────────────────────────────┐
│                    REST Controller Layer                     │
│                    (OrderController.java)                    │
│  • POST   /api/orders                                       │
│  • GET    /api/orders/{id}                                  │
│  • GET    /api/orders?status=X                              │
│  • PUT    /api/orders/{id}/status                           │
│  • POST   /api/orders/{id}/cancel                           │
└──────────────────────────┬──────────────────────────────────┘
                           │
                           ▼
┌─────────────────────────────────────────────────────────────┐
│                      Service Layer                           │
│                    (OrderService.java)                       │
│  • Business Logic                                           │
│  • Order Validation                                         │
│  • Status Transition Rules                                  │
│  • Total Calculation                                        │
└──────────────────────────┬──────────────────────────────────┘
                           │
                           ▼
┌─────────────────────────────────────────────────────────────┐
│                    Repository Layer                          │
│                  (OrderRepository.java)                      │
│  • Spring Data JPA                                          │
│  • Database Operations (CRUD)                               │
│  • Query Methods                                            │
└──────────────────────────┬──────────────────────────────────┘
                           │
                           ▼
┌─────────────────────────────────────────────────────────────┐
│                      H2 Database                             │
│  • In-Memory Database                                       │
│  • Tables: orders, order_items                              │
└─────────────────────────────────────────────────────────────┘


                     ┌────────────────────┐
                     │  Background Jobs   │
                     │ (OrderScheduler)   │
                     │  Runs every 5 min  │
                     └─────────┬──────────┘
                               │
                               ▼
                    Calls OrderService
                 (processPendingOrders)
```

## 2. Order Status State Machine

```
                    ┌──────────────────────────────────┐
                    │     Order Created                │
                    │   (Initial State: PENDING)       │
                    └─────────────┬────────────────────┘
                                  │
                    ┌─────────────┴─────────────┐
                    │                           │
                    │                           │
         ┌──────────▼──────────┐    ┌──────────▼──────────┐
         │   Auto-Scheduler     │    │  Manual Cancellation│
         │   (Every 5 min)      │    │   (Customer Action) │
         └──────────┬───────────┘    └──────────┬──────────┘
                    │                           │
                    │                           │
         ┌──────────▼──────────┐    ┌──────────▼──────────┐
         │    PROCESSING        │    │     CANCELLED       │
         │                      │    │  (Terminal State)   │
         └──────────┬───────────┘    └─────────────────────┘
                    │
                    │ Manual Status Update
                    │
         ┌──────────▼──────────┐
         │      SHIPPED         │
         │                      │
         └──────────┬───────────┘
                    │
                    │ Manual Status Update
                    │
         ┌──────────▼──────────┐
         │     DELIVERED        │
         │  (Terminal State)    │
         └──────────────────────┘
```

## 3. Order Creation Flow

```
Client                Controller           Service              Repository         Database
  │                       │                   │                     │                  │
  │  POST /api/orders     │                   │                     │                  │
  ├──────────────────────►│                   │                     │                  │
  │                       │                   │                     │                  │
  │                       │ createOrder()     │                     │                  │
  │                       ├──────────────────►│                     │                  │
  │                       │                   │                     │                  │
  │                       │                   │ Validate Request    │                  │
  │                       │                   ├────────────┐        │                  │
  │                       │                   │            │        │                  │
  │                       │                   │◄───────────┘        │                  │
  │                       │                   │                     │                  │
  │                       │                   │ Map DTO to Entity   │                  │
  │                       │                   ├────────────┐        │                  │
  │                       │                   │            │        │                  │
  │                       │                   │◄───────────┘        │                  │
  │                       │                   │                     │                  │
  │                       │                   │ Calculate Total     │                  │
  │                       │                   ├────────────┐        │                  │
  │                       │                   │            │        │                  │
  │                       │                   │◄───────────┘        │                  │
  │                       │                   │                     │                  │
  │                       │                   │  save(order)        │                  │
  │                       │                   ├────────────────────►│                  │
  │                       │                   │                     │                  │
  │                       │                   │                     │  INSERT INTO     │
  │                       │                   │                     │  orders, items   │
  │                       │                   │                     ├─────────────────►│
  │                       │                   │                     │                  │
  │                       │                   │                     │  Order + ID      │
  │                       │                   │                     │◄─────────────────┤
  │                       │                   │                     │                  │
  │                       │                   │  Saved Order        │                  │
  │                       │                   │◄────────────────────┤                  │
  │                       │                   │                     │                  │
  │                       │                   │ Map Entity to DTO   │                  │
  │                       │                   ├────────────┐        │                  │
  │                       │                   │            │        │                  │
  │                       │                   │◄───────────┘        │                  │
  │                       │                   │                     │                  │
  │                       │  OrderResponse    │                     │                  │
  │                       │◄──────────────────┤                     │                  │
  │                       │                   │                     │                  │
  │  201 Created          │                   │                     │                  │
  │  + Order JSON         │                   │                     │                  │
  │◄──────────────────────┤                   │                     │                  │
  │                       │                   │                     │                  │
```

## 4. Background Scheduler Flow

```
┌────────────────────────────────────────────────────────┐
│              Spring Scheduler                          │
│         (Every 5 minutes - 300000ms)                   │
└───────────────────────┬────────────────────────────────┘
                        │
                        │ @Scheduled(fixedRate = 300000)
                        │
                        ▼
┌────────────────────────────────────────────────────────┐
│          OrderScheduler.processPendingOrders()         │
│                                                         │
│  1. Log: "Running scheduled task"                      │
│  2. Call service.processPendingOrders()                │
│  3. Log: "Processed X orders"                          │
│  4. Handle exceptions                                  │
└───────────────────────┬────────────────────────────────┘
                        │
                        ▼
┌────────────────────────────────────────────────────────┐
│        OrderService.processPendingOrders()             │
│                                                         │
│  1. Find all orders with status = PENDING              │
│  2. For each order:                                    │
│     • Set status = PROCESSING                          │
│     • Save to database                                 │
│  3. Return count of processed orders                   │
└───────────────────────┬────────────────────────────────┘
                        │
                        ▼
┌────────────────────────────────────────────────────────┐
│             OrderRepository                             │
│                                                         │
│  • findByStatus(PENDING)                               │
│  • save(order)                                         │
└───────────────────────┬────────────────────────────────┘
                        │
                        ▼
                   Database Update
                (PENDING → PROCESSING)
```

## 5. Exception Handling Flow

```
Controller                GlobalExceptionHandler         Client
    │                              │                        │
    │  Exception thrown            │                        │
    ├─────────────────────────────►│                        │
    │                              │                        │
    │                              │ Catch Exception        │
    │                              │                        │
    │                              │ OrderNotFoundException │
    │                              ├──────────┐             │
    │                              │          │ 404         │
    │                              │◄─────────┘             │
    │                              │                        │
    │                              │ InvalidOperationException
    │                              ├──────────┐             │
    │                              │          │ 400         │
    │                              │◄─────────┘             │
    │                              │                        │
    │                              │ ValidationException    │
    │                              ├──────────┐             │
    │                              │          │ 400         │
    │                              │◄─────────┘             │
    │                              │                        │
    │                              │ Build ErrorResponse    │
    │                              ├────────────┐           │
    │                              │            │           │
    │                              │◄───────────┘           │
    │                              │                        │
    │                              │ ErrorResponse JSON     │
    │                              ├───────────────────────►│
    │                              │                        │
```

## 6. Database Schema

```
┌─────────────────────────────────────────┐
│             orders                       │
├─────────────────────────────────────────┤
│ id              BIGINT (PK)             │
│ customer_name   VARCHAR(255)            │
│ customer_email  VARCHAR(255)            │
│ status          VARCHAR(50)             │
│ total_amount    DECIMAL(10,2)           │
│ created_at      TIMESTAMP               │
│ updated_at      TIMESTAMP               │
└─────────────────┬───────────────────────┘
                  │
                  │ 1:N relationship
                  │
                  ▼
┌─────────────────────────────────────────┐
│           order_items                    │
├─────────────────────────────────────────┤
│ id              BIGINT (PK)             │
│ order_id        BIGINT (FK)             │───┐
│ product_name    VARCHAR(255)            │   │
│ quantity        INTEGER                 │   │
│ price           DECIMAL(10,2)           │   │
└─────────────────────────────────────────┘   │
                                              │
                  ┌───────────────────────────┘
                  │
                  │ Foreign Key Constraint
                  │ CASCADE DELETE
                  │
                  ▼
            References orders(id)
```

## 7. Testing Strategy

```
┌──────────────────────────────────────────────────────────┐
│                    Testing Pyramid                        │
└──────────────────────────────────────────────────────────┘

                        ▲
                       ╱ ╲
                      ╱   ╲
                     ╱     ╲
                    ╱       ╲
                   ╱─────────╲
                  ╱  E2E/Full ╲
                 ╱  Integration╲
                ╱───────────────╲
               ╱  Integration    ╲
              ╱  (Controller)     ╲
             ╱─────────────────────╲
            ╱    Unit Tests         ╲
           ╱    (Service Layer)      ╲
          ╱───────────────────────────╲


1. Unit Tests (OrderServiceTest)
   • Test individual methods
   • Mock dependencies
   • Fast execution

2. Integration Tests (OrderControllerTest)
   • Test controller endpoints
   • Mock service layer
   • Validate HTTP responses

3. Full Integration Tests (OrderProcessingIntegrationTest)
   • Test complete workflows
   • Real database (H2)
   • End-to-end scenarios
```

---

## Component Dependencies

```
OrderController
    │
    ├──► OrderService
    │       │
    │       ├──► OrderRepository
    │       │       │
    │       │       └──► Spring Data JPA
    │       │
    │       └──► OrderMapper
    │
    └──► GlobalExceptionHandler


OrderScheduler
    │
    └──► OrderService
            │
            └──► (same as above)
```

---

## Request/Response Flow with Validation

```
1. Request Arrives
   ↓
2. @Valid Annotation Triggers Validation
   ↓
3. If Invalid → MethodArgumentNotValidException
   ↓
4. GlobalExceptionHandler Catches It
   ↓
5. Returns 400 with Detailed Error Messages

6. If Valid → Controller Method Executes
   ↓
7. Service Layer Processing
   ↓
8. Repository Database Operation
   ↓
9. Success Response or Exception
   ↓
10. Exception → GlobalExceptionHandler
   ↓
11. Client Receives Response (Success or Error)
```
