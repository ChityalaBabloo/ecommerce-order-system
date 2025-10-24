package com.ecommerce.orderprocessing.controller;

import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * Home Controller for API information
 */
@RestController
public class HomeController {
    
    @GetMapping("/")
    @Hidden  // Hide from Swagger UI
    public ResponseEntity<Map<String, Object>> home() {
        Map<String, Object> response = new HashMap<>();
        response.put("application", "E-commerce Order Processing System");
        response.put("version", "1.0.0");
        response.put("status", "Running");
        response.put("apiEndpoints", Map.of(
            "orders", "/api/orders",
            "createOrder", "POST /api/orders",
            "getOrder", "GET /api/orders/{id}",
            "getAllOrders", "GET /api/orders",
            "updateStatus", "PUT /api/orders/{id}/status",
            "cancelOrder", "POST /api/orders/{id}/cancel"
        ));
        response.put("swagger", "/swagger-ui.html");
        response.put("apiDocs", "/api-docs");
        response.put("h2Console", "/h2-console");
        response.put("documentation", "Visit /swagger-ui.html for interactive API documentation");
        
        return ResponseEntity.ok(response);
    }
}
