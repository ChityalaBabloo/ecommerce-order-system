# E-commerce Order Processing System

A robust backend system built with Spring Boot for managing e-commerce orders. The system provides RESTful APIs for order creation, tracking, status management, and automatic order processing.
The high-level architecture of the system is documented in [ARCHITECTURE.md](ARCHITECTURE.md).

## 🎯 Scope and Assumptions

This system focuses on **order processing and management**. The following aspects are **out of scope** for this assignment:
- ❌ Customer authentication and authorization
- ❌ User management and registration
- ❌ Customer identity verification
- ❌ Payment processing
- ❌ Inventory management

**Assumptions**:
- All customers are considered valid and authorized
- Customer name and email are informational fields only (not for authentication)
- The same customer can place multiple orders
- Focus is on order lifecycle management: creation, status updates, cancellation, and automated processing

## 📋 Table of Contents
- [Features](#-features)
- [Technology Stack](#-technology-stack)
- [Prerequisites](#-prerequisites)
- [Installation](#-installation)
- [Running the Application](#️-running-the-application)
- [API Documentation](#-api-documentation)
- [Database](#-database)
- [Testing](#-testing)
- [Project Structure](#-project-structure)
- [Order Status Flow](#-order-status-flow)
- [Troubleshooting](#-troubleshooting)

## ✨ Features

### Core Functionality
- ✅ **Create Orders**: Customers can place orders with multiple items
- ✅ **Retrieve Order Details**: Fetch order information by order ID
- ✅ **Update Order Status**: Manage order lifecycle with statuses: `PENDING`, `PROCESSING`, `SHIPPED`, `DELIVERED`
- ✅ **List All Orders**: Retrieve all orders with optional status filtering
- ✅ **Cancel Orders**: Customers can cancel orders (only when status is `PENDING`)
- ✅ **Automatic Processing**: Background job automatically updates `PENDING` orders to `PROCESSING` every 5 minutes

### Additional Features
- Input validation with detailed error messages
- Global exception handling
- Comprehensive unit and integration tests
- H2 in-memory database for quick setup
- RESTful API design
- Scheduled task execution

## 🛠 Technology Stack

- **Java**: 17
- **Spring Boot**: 3.1.5
- **Spring Data JPA**: For database operations
- **Spring Web**: RESTful API
- **Spring Validation**: Input validation
- **H2 Database**: In-memory database
- **Lombok**: Reduce boilerplate code
- **JUnit 5**: Unit testing
- **Mockito**: Mocking framework
- **Maven**: Build tool

## 📦 Prerequisites

- Java 17 or higher
- Maven 3.6 or higher

## 🚀 Installation

1. **Clone or download the project**:
   ```bash
   cd /tmp/ecommerce-order-system
   ```

2. **Build the project**:
   ```bash
   mvn clean install
   ```

## ▶️ Running the Application

### Option 1: Using Maven
```bash
mvn spring-boot:run
```

### Option 2: Using Java
```bash
mvn clean package
java -jar target/order-processing-system-1.0.0.jar
```


### Access the Application

- **Application URL**: <http://localhost:8080> (root endpoint with API details)
- **H2 Console** (for database inspection): <http://localhost:8080/h2-console> 
- (Click on the above link to access h2-databse. use the following credentials and click "Connect" to access the database.)
  - JDBC URL: `jdbc:h2:mem:orderdb`
  - Username: `sa`
  - Password: (leave empty)

## 📚 API Documentation

### Interactive API Documentation (Swagger UI)

Once the application is running, you can access the **interactive Swagger UI** at:
<http://localhost:8080/swagger-ui.html>

### Manual API Testing

### Base URL
```
http://localhost:8080/api/orders
```

### 1. Create Order
**POST** `/api/orders`

**Request Body**:
```json
{
  "customerName": "John Doe",
  "customerEmail": "john@example.com",
  "items": [
    {
      "productName": "Laptop",
      "quantity": 1,
      "price": 999.99
    },
    {
      "productName": "Mouse",
      "quantity": 2,
      "price": 25.50
    }
  ]
}
```

**Response** (201 Created):
```json
{
  "id": 1,
  "customerName": "John Doe",
  "customerEmail": "john@example.com",
  "status": "PENDING",
  "items": [
    {
      "id": 1,
      "productName": "Laptop",
      "quantity": 1,
      "price": 999.99,
      "subtotal": 999.99
    },
    {
      "id": 2,
      "productName": "Mouse",
      "quantity": 2,
      "price": 25.50,
      "subtotal": 51.00
    }
  ],
  "totalAmount": 1050.99,
  "createdAt": "2025-10-23T10:30:00",
  "updatedAt": "2025-10-23T10:30:00"
}
```

### 2. Get Order by ID
**GET** `/api/orders/{id}`

**Response** (200 OK):
```json
{
  "id": 1,
  "customerName": "John Doe",
  "customerEmail": "john@example.com",
  "status": "PENDING",
  "items": [...],
  "totalAmount": 1050.99,
  "createdAt": "2025-10-23T10:30:00",
  "updatedAt": "2025-10-23T10:30:00"
}
```

**Error Response** (404 Not Found):
```json
{
  "timestamp": "2025-10-23T10:35:00",
  "status": 404,
  "error": "Not Found",
  "message": "Order not found with id: 999",
  "path": "/api/orders/999"
}
```

### 3. Get All Orders
**GET** `/api/orders`

**Optional Query Parameters**:
- `status`: Filter by order status (PENDING, PROCESSING, SHIPPED, DELIVERED, CANCELLED)

**Examples**:
- Get all orders: `GET /api/orders`
- Get pending orders: `GET /api/orders?status=PENDING`
- Get shipped orders: `GET /api/orders?status=SHIPPED`

**Response** (200 OK):
```json
[
  {
    "id": 1,
    "customerName": "John Doe",
    "customerEmail": "john@example.com",
    "status": "PENDING",
    "items": [...],
    "totalAmount": 1050.99,
    "createdAt": "2025-10-23T10:30:00",
    "updatedAt": "2025-10-23T10:30:00"
  }
]
```

### 4. Update Order Status
**PUT** `/api/orders/{id}/status?status={NEW_STATUS}`

**Query Parameters**:
- `status`: New status (PENDING, PROCESSING, SHIPPED, DELIVERED)

**Example**: `PUT /api/orders/1/status?status=PROCESSING`

**Response** (200 OK):
```json
{
  "id": 1,
  "customerName": "John Doe",
  "customerEmail": "john@example.com",
  "status": "PROCESSING",
  ...
}
```

**Error Response** (400 Bad Request):
```json
{
  "timestamp": "2025-10-23T10:40:00",
  "status": 400,
  "error": "Bad Request",
  "message": "PROCESSING orders can only move to SHIPPED",
  "path": "/api/orders/1/status"
}
```

### 5. Cancel Order
**POST** `/api/orders/{id}/cancel`

**Response** (200 OK):
```json
{
  "id": 1,
  "customerName": "John Doe",
  "customerEmail": "john@example.com",
  "status": "CANCELLED",
  ...
}
```

**Error Response** (400 Bad Request):
```json
{
  "timestamp": "2025-10-23T10:45:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Order cannot be cancelled. Current status: SHIPPED",
  "path": "/api/orders/1/cancel"
}
```

## 💾 Database

The application uses H2 in-memory database with the following schema:

### Orders Table
| Column | Type | Description |
|--------|------|-------------|
| id | BIGINT | Primary key |
| customer_name | VARCHAR | Customer's name |
| customer_email | VARCHAR | Customer's email |
| status | VARCHAR | Order status (PENDING, PROCESSING, SHIPPED, DELIVERED, CANCELLED) |
| total_amount | DECIMAL(10,2) | Total order amount |
| created_at | TIMESTAMP | Creation timestamp |
| updated_at | TIMESTAMP | Last update timestamp |

### Order Items Table
| Column | Type | Description |
|--------|------|-------------|
| id | BIGINT | Primary key |
| order_id | BIGINT | Foreign key to orders |
| product_name | VARCHAR | Product name |
| quantity | INTEGER | Item quantity |
| price | DECIMAL(10,2) | Item price |

## 🧪 Testing

### Code Coverage with JaCoCo

Generate code coverage report:
```bash
mvn clean test
```

After running the above command, an `index.html` file gets generated at `target/site/jacoco/index.html`. Open it in a browser to see the code coverage report.

**On macOS:**
```bash
open target/site/jacoco/index.html
```



### Test Coverage

Unit tests have been written for the following files:
- **OrderServiceTest**: Business logic testing
- **OrderControllerTest**: REST endpoint testing  
- **OrderProcessingIntegrationTest**: Full integration testing

To test the application features through APIs, follow [API_TESTING_GUIDE.md](API_TESTING_GUIDE.md).

To test from Postman, use the [Order_Processing_API.postman_collection.json](Order_Processing_API.postman_collection.json) collection. Import it into Postman and start testing the application.

## 📁 Project Structure

```
ecommerce-order-system/
├── src/
│   ├── main/
│   │   ├── java/com/ecommerce/orderprocessing/
│   │   │   ├── controller/
│   │   │   │   ├── HomeController.java           # Root endpoint (API info)
│   │   │   │   └── OrderController.java          # REST API endpoints
│   │   │   ├── dto/
│   │   │   │   ├── OrderRequest.java             # Request DTO
│   │   │   │   ├── OrderResponse.java            # Response DTO
│   │   │   │   ├── OrderItemRequest.java
│   │   │   │   └── OrderItemResponse.java
│   │   │   ├── exception/
│   │   │   │   ├── GlobalExceptionHandler.java   # Global error handling
│   │   │   │   ├── OrderNotFoundException.java
│   │   │   │   ├── InvalidOrderOperationException.java
│   │   │   │   └── ErrorResponse.java
│   │   │   ├── mapper/
│   │   │   │   └── OrderMapper.java              # Entity-DTO mapping
│   │   │   ├── model/
│   │   │   │   ├── Order.java                    # Order entity
│   │   │   │   ├── OrderItem.java                # Order item entity
│   │   │   │   └── OrderStatus.java              # Status enum
│   │   │   ├── repository/
│   │   │   │   └── OrderRepository.java          # Data access layer
│   │   │   ├── scheduler/
│   │   │   │   └── OrderScheduler.java           # Background job
│   │   │   ├── service/
│   │   │   │   └── OrderService.java             # Business logic
│   │   │   ├── config/
│   │   │   │   └── DataLoader.java               # Sample data loader (dev)
│   │   │   └── OrderProcessingApplication.java   # Main class
│   │   └── resources/
│   │       ├── application.properties            # Main configuration (profile selection)
│   │       └── application-dev.properties        # Dev profile configuration
│   └── test/
│       ├── java/com/ecommerce/orderprocessing/
│       │   ├── OrderProcessingIntegrationTest.java  # Full integration tests
│       │   ├── controller/
│       │   │   └── OrderControllerTest.java
│       │   └── service/
│       │       └── OrderServiceTest.java
│       └── resources/
│           └── application.properties
├── pom.xml                                        # Maven configuration
├── README.md                                      # This file
├── API_TESTING_GUIDE.md                           # API testing scenarios
├── ARCHITECTURE.md                                # System architecture
└── Order_Processing_API.postman_collection.json  # Postman collection
```

## 🔄 Order Status Flow

```
PENDING ──────► PROCESSING ──────► SHIPPED ──────► DELIVERED
   │
   └──────────► CANCELLED
```

**Rules**:
- Orders start in `PENDING` status
- Only `PENDING` orders can be cancelled
- Background job automatically moves `PENDING` → `PROCESSING` every 5 minutes
- Status transitions must follow the flow: `PENDING` → `PROCESSING` → `SHIPPED` → `DELIVERED`
- `CANCELLED` and `DELIVERED` are terminal states

## 🐛 Troubleshooting

### Port Already in Use
If port 8080 is already in use, change it in `application.properties`:
```properties
server.port=8081
```

### Build Failures
Ensure you have Java 17 and Maven installed:
```bash
java -version
mvn -version
```

## 📝 Note

1. I have documented this README (other .md files) with assistance from ChatGPT for content structuring and formatting.
2. Due to time constraints, Docker and Jenkins files are not included in this implementation.

## 👨‍💻 Author

Dharma Teja Chityala
