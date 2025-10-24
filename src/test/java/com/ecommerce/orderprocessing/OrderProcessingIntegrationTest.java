package com.ecommerce.orderprocessing;

import com.ecommerce.orderprocessing.dto.OrderItemRequest;
import com.ecommerce.orderprocessing.dto.OrderRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for the entire Order Processing System
 */
@SpringBootTest
@AutoConfigureMockMvc
class OrderProcessingIntegrationTest {
    
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void completeOrderLifecycle_Success() throws Exception {
        // 1. Create an order
        OrderItemRequest item = OrderItemRequest.builder()
                .productName("Integration Test Product")
                .quantity(3)
                .price(new BigDecimal("33.33"))
                .build();
        
        OrderRequest orderRequest = OrderRequest.builder()
                .customerName("Integration Test User")
                .customerEmail("integration@test.com")
                .items(Collections.singletonList(item))
                .build();
        
        MvcResult createResult = mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.customerName").value("Integration Test User"))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.totalAmount").value(99.99))
                .andReturn();
        
        String responseBody = createResult.getResponse().getContentAsString();
        Long orderId = objectMapper.readTree(responseBody).get("id").asLong();
        
        // 2. Retrieve the order
        mockMvc.perform(get("/api/orders/" + orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(orderId))
                .andExpect(jsonPath("$.status").value("PENDING"));
        
        // 3. Update status to PROCESSING
        mockMvc.perform(put("/api/orders/" + orderId + "/status?status=PROCESSING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PROCESSING"));
        
        // 4. Update status to SHIPPED
        mockMvc.perform(put("/api/orders/" + orderId + "/status?status=SHIPPED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SHIPPED"));
        
        // 5. Update status to DELIVERED
        mockMvc.perform(put("/api/orders/" + orderId + "/status?status=DELIVERED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("DELIVERED"));
        
        // 6. Verify final state
        mockMvc.perform(get("/api/orders/" + orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("DELIVERED"));
    }
    
    @Test
    void orderCancellation_Success() throws Exception {
        // Create an order
        OrderItemRequest item = OrderItemRequest.builder()
                .productName("Cancellation Test Product")
                .quantity(1)
                .price(new BigDecimal("100.00"))
                .build();
        
        OrderRequest orderRequest = OrderRequest.builder()
                .customerName("Cancel Test User")
                .customerEmail("cancel@test.com")
                .items(Collections.singletonList(item))
                .build();
        
        MvcResult createResult = mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderRequest)))
                .andExpect(status().isCreated())
                .andReturn();
        
        String responseBody = createResult.getResponse().getContentAsString();
        long orderId = objectMapper.readTree(responseBody).get("id").asLong();
        
        // Cancel the order (should succeed as it's PENDING)
        mockMvc.perform(post("/api/orders/" + orderId + "/cancel"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));
    }
    
    @Test
    void orderCancellation_Failure_WhenNotPending() throws Exception {
        // Create and process an order
        OrderItemRequest item = OrderItemRequest.builder()
                .productName("No Cancel Test Product")
                .quantity(1)
                .price(new BigDecimal("50.00"))
                .build();
        
        OrderRequest orderRequest = OrderRequest.builder()
                .customerName("No Cancel Test User")
                .customerEmail("nocancel@test.com")
                .items(Collections.singletonList(item))
                .build();
        
        MvcResult createResult = mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderRequest)))
                .andExpect(status().isCreated())
                .andReturn();
        
        String responseBody = createResult.getResponse().getContentAsString();
        long orderId = objectMapper.readTree(responseBody).get("id").asLong();
        
        // Move to PROCESSING
        mockMvc.perform(put("/api/orders/" + orderId + "/status?status=PROCESSING"))
                .andExpect(status().isOk());
        
        // Try to cancel (should fail)
        mockMvc.perform(post("/api/orders/" + orderId + "/cancel"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Order cannot be cancelled. Current status: PROCESSING"));
    }
    
    @Test
    void getAllOrders_WithStatusFilter() throws Exception {
        // Create multiple orders
        for (int i = 0; i < 3; i++) {
            OrderItemRequest item = OrderItemRequest.builder()
                    .productName("Product " + i)
                    .quantity(1)
                    .price(new BigDecimal("10.00"))
                    .build();
            
            OrderRequest orderRequest = OrderRequest.builder()
                    .customerName("User " + i)
                    .customerEmail("user" + i + "@test.com")
                    .items(Collections.singletonList(item))
                    .build();
            
            mockMvc.perform(post("/api/orders")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(orderRequest)))
                    .andExpect(status().isCreated());
        }
        
        // Get all PENDING orders
        mockMvc.perform(get("/api/orders?status=PENDING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].status").value("PENDING"));
    }
    
    @Test
    void createOrder_ValidationFailure() throws Exception {
        // Empty customer name
        OrderRequest invalidRequest = OrderRequest.builder()
                .customerName("")
                .customerEmail("invalid@test.com")
                .items(List.of())
                .build();
        
        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Failed"));
    }
    
    @Test
    void getOrder_NotFound() throws Exception {
        mockMvc.perform(get("/api/orders/99999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Order not found with id: 99999"));
    }
    
    @Test
    void updateOrderStatus_InvalidTransition() throws Exception {
        // Create an order
        OrderItemRequest item = OrderItemRequest.builder()
                .productName("Invalid Transition Test")
                .quantity(1)
                .price(new BigDecimal("25.00"))
                .build();
        
        OrderRequest orderRequest = OrderRequest.builder()
                .customerName("Transition Test User")
                .customerEmail("transition@test.com")
                .items(Collections.singletonList(item))
                .build();
        
        MvcResult createResult = mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderRequest)))
                .andExpect(status().isCreated())
                .andReturn();
        
        String responseBody = createResult.getResponse().getContentAsString();
        long orderId = objectMapper.readTree(responseBody).get("id").asLong();
        
        // Try to jump from PENDING to SHIPPED (invalid transition)
        mockMvc.perform(put("/api/orders/" + orderId + "/status?status=SHIPPED"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("PENDING orders can only move to PROCESSING or be CANCELLED"));
    }
}
