package com.ecommerce.orderprocessing.mapper;

import com.ecommerce.orderprocessing.dto.OrderItemRequest;
import com.ecommerce.orderprocessing.dto.OrderItemResponse;
import com.ecommerce.orderprocessing.dto.OrderRequest;
import com.ecommerce.orderprocessing.dto.OrderResponse;
import com.ecommerce.orderprocessing.model.Order;
import com.ecommerce.orderprocessing.model.OrderItem;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

/**
 * Mapper utility to convert between entities and DTOs
 */
@Component
public class OrderMapper {
    
    /**
     * Convert OrderRequest to Order entity
     */
    public Order toEntity(OrderRequest request) {
        Order order = Order.builder()
                .customerName(request.getCustomerName())
                .customerEmail(request.getCustomerEmail())
                .build();
        
        // Convert and add items
        request.getItems().forEach(itemRequest -> {
            OrderItem item = OrderItem.builder()
                    .productName(itemRequest.getProductName())
                    .quantity(itemRequest.getQuantity())
                    .price(itemRequest.getPrice())
                    .build();
            order.addItem(item);
        });
        
        // Calculate total
        order.calculateTotalAmount();
        
        return order;
    }
    
    /**
     * Convert Order entity to OrderResponse
     */
    public OrderResponse toResponse(Order order) {
        return OrderResponse.builder()
                .id(order.getId())
                .customerName(order.getCustomerName())
                .customerEmail(order.getCustomerEmail())
                .status(order.getStatus())
                .items(order.getItems().stream()
                        .map(this::toItemResponse)
                        .collect(Collectors.toList()))
                .totalAmount(order.getTotalAmount())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }
    
    /**
     * Convert OrderItem entity to OrderItemResponse
     */
    private OrderItemResponse toItemResponse(OrderItem item) {
        return OrderItemResponse.builder()
                .id(item.getId())
                .productName(item.getProductName())
                .quantity(item.getQuantity())
                .price(item.getPrice())
                .subtotal(item.getSubtotal())
                .build();
    }
}
