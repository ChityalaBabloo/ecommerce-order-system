package com.ecommerce.orderprocessing.config;

import com.ecommerce.orderprocessing.model.Order;
import com.ecommerce.orderprocessing.model.OrderItem;
import com.ecommerce.orderprocessing.model.OrderStatus;
import com.ecommerce.orderprocessing.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.math.BigDecimal;

/**
 * Configuration class to load sample data for testing
 * Only active in 'dev' profile
 */
@Configuration
@RequiredArgsConstructor
@Slf4j
public class DataLoader {
    
    @Bean
    @Profile("dev")
    public CommandLineRunner loadData(OrderRepository orderRepository) {
        return args -> {
            log.info("Loading sample data...");
            
            // Sample Order 1
            Order order1 = Order.builder()
                    .customerName("Alice Johnson")
                    .customerEmail("alice@example.com")
                    .status(OrderStatus.PENDING)
                    .build();
            
            OrderItem item1 = OrderItem.builder()
                    .productName("Laptop")
                    .quantity(1)
                    .price(new BigDecimal("1299.99"))
                    .build();
            
            OrderItem item2 = OrderItem.builder()
                    .productName("Wireless Mouse")
                    .quantity(2)
                    .price(new BigDecimal("29.99"))
                    .build();
            
            order1.addItem(item1);
            order1.addItem(item2);
            order1.calculateTotalAmount();
            orderRepository.save(order1);
            
            // Sample Order 2
            Order order2 = Order.builder()
                    .customerName("Bob Smith")
                    .customerEmail("bob@example.com")
                    .status(OrderStatus.PROCESSING)
                    .build();
            
            OrderItem item3 = OrderItem.builder()
                    .productName("Smartphone")
                    .quantity(1)
                    .price(new BigDecimal("899.99"))
                    .build();
            
            order2.addItem(item3);
            order2.calculateTotalAmount();
            orderRepository.save(order2);
            
            // Sample Order 3
            Order order3 = Order.builder()
                    .customerName("Carol White")
                    .customerEmail("carol@example.com")
                    .status(OrderStatus.SHIPPED)
                    .build();
            
            OrderItem item4 = OrderItem.builder()
                    .productName("Headphones")
                    .quantity(1)
                    .price(new BigDecimal("199.99"))
                    .build();
            
            order3.addItem(item4);
            order3.calculateTotalAmount();
            orderRepository.save(order3);
            
            log.info("Sample data loaded successfully!");
        };
    }
}
