package com.ecommerce.orderprocessing.scheduler;

import com.ecommerce.orderprocessing.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Scheduler for automatic order processing tasks
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class OrderScheduler {
    
    private final OrderService orderService;
    
    /**
     * Automatically process PENDING orders every 5 minutes
     * Changes order status from PENDING to PROCESSING
     */
    @Scheduled(fixedRate = 300000) // 5 minutes = 300,000 milliseconds
    public void processPendingOrders() {
        log.info("Running scheduled task: Process pending orders");
        
        try {
            int processedCount = orderService.processPendingOrders();
            log.info("Scheduled task completed. Processed {} orders", processedCount);
        } catch (Exception e) {
            log.error("Error processing pending orders: {}", e.getMessage(), e);
        }
    }
}
