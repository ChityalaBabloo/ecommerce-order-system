package com.ecommerce.orderprocessing.repository;

import com.ecommerce.orderprocessing.model.Order;
import com.ecommerce.orderprocessing.model.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for Order entity
 */
@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    
    /**
     * Find all orders with a specific status
     */
    List<Order> findByStatus(OrderStatus status);
    
    /**
     * Find all orders by customer email
     */
    List<Order> findByCustomerEmail(String customerEmail);
}
