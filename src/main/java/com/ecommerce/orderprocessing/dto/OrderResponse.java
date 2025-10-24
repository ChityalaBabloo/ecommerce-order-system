package com.ecommerce.orderprocessing.dto;

import com.ecommerce.orderprocessing.model.OrderStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Order response with complete order details")
public class OrderResponse {
    
    @Schema(description = "Unique order identifier", example = "1")
    private Long id;
    
    @Schema(description = "Customer's full name", example = "John Doe")
    private String customerName;
    
    @Schema(description = "Customer's email address", example = "john.doe@example.com")
    private String customerEmail;
    
    @Schema(description = "Current order status", example = "PENDING")
    private OrderStatus status;
    
    @Schema(description = "List of items in the order")
    private List<OrderItemResponse> items;
    
    @Schema(description = "Total order amount", example = "1999.98")
    private BigDecimal totalAmount;
    
    @Schema(description = "Order creation timestamp", example = "2025-10-24T10:30:00")
    private LocalDateTime createdAt;
    
    @Schema(description = "Order last update timestamp", example = "2025-10-24T10:30:00")
    private LocalDateTime updatedAt;
}
