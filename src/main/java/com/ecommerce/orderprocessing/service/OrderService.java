package com.ecommerce.orderprocessing.service;

import com.ecommerce.orderprocessing.dto.OrderRequest;
import com.ecommerce.orderprocessing.dto.OrderResponse;
import com.ecommerce.orderprocessing.exception.InvalidOrderOperationException;
import com.ecommerce.orderprocessing.exception.OrderNotFoundException;
import com.ecommerce.orderprocessing.mapper.OrderMapper;
import com.ecommerce.orderprocessing.model.Order;
import com.ecommerce.orderprocessing.model.OrderStatus;
import com.ecommerce.orderprocessing.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service class for managing orders
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {
    
    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    

    @Transactional
    public OrderResponse createOrder(OrderRequest request) {
        log.info("Creating new order for customer: {}", request.getCustomerName());
        
        Order order = orderMapper.toEntity(request);
        Order savedOrder = orderRepository.save(order);
        
        log.info("Order created successfully with ID: {}", savedOrder.getId());
        return orderMapper.toResponse(savedOrder);
    }
    

    @Transactional(readOnly = true)
    public OrderResponse getOrderById(Long orderId) {
        log.info("Fetching order with ID: {}", orderId);
        
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));
        
        return orderMapper.toResponse(order);
    }
    
    /**
     * Get all orders, optionally filtered by status
     */
    @Transactional(readOnly = true)
    public List<OrderResponse> getAllOrders(OrderStatus status) {
        log.info("Fetching all orders with status: {}", status);
        
        List<Order> orders;
        if (status != null) {
            orders = orderRepository.findByStatus(status);
        } else {
            orders = orderRepository.findAll();
        }
        
        return orders.stream()
                .map(orderMapper::toResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * Update order status
     */
    @Transactional
    public OrderResponse updateOrderStatus(Long orderId, OrderStatus newStatus) {
        log.info("Updating order {} status to: {}", orderId, newStatus);
        
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));
        
        // Validate status transition
        validateStatusTransition(order.getStatus(), newStatus);
        
        order.setStatus(newStatus);
        Order updatedOrder = orderRepository.save(order);
        
        log.info("Order {} status updated successfully", orderId);
        return orderMapper.toResponse(updatedOrder);
    }
    
    /**
     * Cancel an order (only if status is PENDING)
     */
    @Transactional
    public OrderResponse cancelOrder(Long orderId) {
        log.info("Attempting to cancel order: {}", orderId);
        
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));
        
        if (!order.canBeCancelled()) {
            throw new InvalidOrderOperationException(
                    "Order cannot be cancelled. Current status: " + order.getStatus()
            );
        }
        
        order.setStatus(OrderStatus.CANCELLED);
        Order cancelledOrder = orderRepository.save(order);
        
        log.info("Order {} cancelled successfully", orderId);
        return orderMapper.toResponse(cancelledOrder);
    }
    
    /**
     * Process pending orders (change status from PENDING to PROCESSING)
     * This method is called by the scheduled task
     */
    @Transactional
    public int processPendingOrders() {
        log.info("Processing pending orders...");
        
        List<Order> pendingOrders = orderRepository.findByStatus(OrderStatus.PENDING);
        
        for (Order order : pendingOrders) {
            order.setStatus(OrderStatus.PROCESSING);
            orderRepository.save(order);
            log.debug("Order {} moved to PROCESSING", order.getId());
        }
        
        log.info("Processed {} pending orders", pendingOrders.size());
        return pendingOrders.size();
    }
    
    /**
     * Validate status transition logic
     */
    private void validateStatusTransition(OrderStatus currentStatus, OrderStatus newStatus) {
        // Cannot change status of cancelled orders
        if (currentStatus == OrderStatus.CANCELLED) {
            throw new InvalidOrderOperationException("Cannot update status of a cancelled order");
        }
        
        // Cannot move back from DELIVERED
        if (currentStatus == OrderStatus.DELIVERED) {
            throw new InvalidOrderOperationException("Cannot update status of a delivered order");
        }
        
        // Validate proper flow: PENDING -> PROCESSING -> SHIPPED -> DELIVERED
        if (currentStatus == OrderStatus.PENDING && 
            newStatus != OrderStatus.PROCESSING && 
            newStatus != OrderStatus.CANCELLED) {
            throw new InvalidOrderOperationException("PENDING orders can only move to PROCESSING or be CANCELLED");
        }
        
        if (currentStatus == OrderStatus.PROCESSING && 
            newStatus != OrderStatus.SHIPPED) {
            throw new InvalidOrderOperationException("PROCESSING orders can only move to SHIPPED");
        }
        
        if (currentStatus == OrderStatus.SHIPPED && 
            newStatus != OrderStatus.DELIVERED) {
            throw new InvalidOrderOperationException("SHIPPED orders can only move to DELIVERED");
        }
    }
}
