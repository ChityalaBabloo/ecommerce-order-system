package com.ecommerce.orderprocessing.controller;

import com.ecommerce.orderprocessing.dto.OrderItemRequest;
import com.ecommerce.orderprocessing.dto.OrderRequest;
import com.ecommerce.orderprocessing.dto.OrderResponse;
import com.ecommerce.orderprocessing.model.OrderStatus;
import com.ecommerce.orderprocessing.service.OrderService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for OrderController
 */
@WebMvcTest(OrderController.class)
class OrderControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @MockitoBean
    private OrderService orderService;
    
    private OrderRequest testOrderRequest;
    private OrderResponse testOrderResponse;
    
    @BeforeEach
    void setUp() {
        OrderItemRequest itemRequest = OrderItemRequest.builder()
                .productName("Product 1")
                .quantity(2)
                .price(new BigDecimal("50.00"))
                .build();
        
        testOrderRequest = OrderRequest.builder()
                .customerName("John Doe")
                .customerEmail("john@example.com")
                .items(Collections.singletonList(itemRequest))
                .build();
        
        testOrderResponse = OrderResponse.builder()
                .id(1L)
                .customerName("John Doe")
                .customerEmail("john@example.com")
                .status(OrderStatus.PENDING)
                .totalAmount(new BigDecimal("100.00"))
                .build();
    }
    
    @Test
    void createOrder_Success() throws Exception {
        when(orderService.createOrder(any(OrderRequest.class))).thenReturn(testOrderResponse);
        
        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testOrderRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.customerName").value("John Doe"))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }
    
    @Test
    void createOrder_InvalidRequest() throws Exception {
        OrderRequest invalidRequest = OrderRequest.builder()
                .customerName("")
                .customerEmail("invalid-email")
                .items(List.of())
                .build();
        
        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    void getOrderById_Success() throws Exception {
        when(orderService.getOrderById(1L)).thenReturn(testOrderResponse);
        
        mockMvc.perform(get("/api/orders/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.customerName").value("John Doe"));
    }
    
    @Test
    void getAllOrders_Success() throws Exception {
        List<OrderResponse> orders = Collections.singletonList(testOrderResponse);
        when(orderService.getAllOrders(null)).thenReturn(orders);
        
        mockMvc.perform(get("/api/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].customerName").value("John Doe"));
    }
    
    @Test
    void getAllOrders_WithStatusFilter() throws Exception {
        List<OrderResponse> orders = Collections.singletonList(testOrderResponse);
        when(orderService.getAllOrders(OrderStatus.PENDING)).thenReturn(orders);
        
        mockMvc.perform(get("/api/orders?status=PENDING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("PENDING"));
    }
    
    @Test
    void updateOrderStatus_Success() throws Exception {
        testOrderResponse.setStatus(OrderStatus.PROCESSING);
        when(orderService.updateOrderStatus(eq(1L), eq(OrderStatus.PROCESSING)))
                .thenReturn(testOrderResponse);
        
        mockMvc.perform(put("/api/orders/1/status?status=PROCESSING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PROCESSING"));
    }
    
    @Test
    void cancelOrder_Success() throws Exception {
        testOrderResponse.setStatus(OrderStatus.CANCELLED);
        when(orderService.cancelOrder(1L)).thenReturn(testOrderResponse);
        
        mockMvc.perform(post("/api/orders/1/cancel"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));
    }
}
