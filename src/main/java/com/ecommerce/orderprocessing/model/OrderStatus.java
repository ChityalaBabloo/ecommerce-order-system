package com.ecommerce.orderprocessing.model;

/**
 * Enum representing the different statuses an order can have
 */
public enum OrderStatus {
    PENDING,
    PROCESSING,
    SHIPPED,
    DELIVERED,
    CANCELLED
}
