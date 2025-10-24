package com.ecommerce.orderprocessing.exception;

/**
 * Exception thrown when an invalid operation is attempted on an order
 */
public class InvalidOrderOperationException extends RuntimeException {
    
    public InvalidOrderOperationException(String message) {
        super(message);
    }
}
