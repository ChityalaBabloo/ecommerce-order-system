package com.ecommerce.orderprocessing.service;

import com.ecommerce.orderprocessing.dto.OrderItemRequest;
import com.ecommerce.orderprocessing.dto.OrderRequest;
import com.ecommerce.orderprocessing.dto.OrderResponse;
import com.ecommerce.orderprocessing.exception.InvalidOrderOperationException;
import com.ecommerce.orderprocessing.exception.OrderNotFoundException;
import com.ecommerce.orderprocessing.mapper.OrderMapper;
import com.ecommerce.orderprocessing.model.Order;
import com.ecommerce.orderprocessing.model.OrderStatus;
import com.ecommerce.orderprocessing.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class OrderServiceTest {
    
    @Mock
    private OrderRepository orderRepository;
    
    @Mock
    private OrderMapper orderMapper;
    
    @InjectMocks
    private OrderService orderService;
    
    private Order testOrder;
    private OrderRequest testOrderRequest;
    private OrderResponse testOrderResponse;
    
    @BeforeEach
    void setUp() {
        testOrder = Order.builder()
                .id(1L)
                .customerName("John Doe")
                .customerEmail("john@example.com")
                .status(OrderStatus.PENDING)
                .totalAmount(new BigDecimal("100.00"))
                .build();
        
        OrderItemRequest itemRequest = OrderItemRequest.builder()
                .productName("Product 1")
                .quantity(2)
                .price(new BigDecimal("50.00"))
                .build();
        
        testOrderRequest = OrderRequest.builder()
                .customerName("John Doe")
                .customerEmail("john@example.com")
                .items(Arrays.asList(itemRequest))
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
    void createOrder_Success() {
        when(orderMapper.toEntity(testOrderRequest)).thenReturn(testOrder);
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);
        when(orderMapper.toResponse(testOrder)).thenReturn(testOrderResponse);
        
        OrderResponse result = orderService.createOrder(testOrderRequest);
        
        assertNotNull(result);
        assertEquals("John Doe", result.getCustomerName());
        verify(orderRepository, times(1)).save(any(Order.class));
    }
    
    @Test
    void getOrderById_Success() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        when(orderMapper.toResponse(testOrder)).thenReturn(testOrderResponse);
        
        OrderResponse result = orderService.getOrderById(1L);
        
        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(orderRepository, times(1)).findById(1L);
    }
    
    @Test
    void getOrderById_NotFound() {
        when(orderRepository.findById(999L)).thenReturn(Optional.empty());
        
        assertThrows(OrderNotFoundException.class, () -> orderService.getOrderById(999L));
    }
    
    @Test
    void getAllOrders_WithoutStatusFilter() {
        List<Order> orders = Arrays.asList(testOrder);
        when(orderRepository.findAll()).thenReturn(orders);
        when(orderMapper.toResponse(any(Order.class))).thenReturn(testOrderResponse);
        
        List<OrderResponse> result = orderService.getAllOrders(null);
        
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(orderRepository, times(1)).findAll();
    }
    
    @Test
    void getAllOrders_WithStatusFilter() {
        List<Order> orders = Arrays.asList(testOrder);
        when(orderRepository.findByStatus(OrderStatus.PENDING)).thenReturn(orders);
        when(orderMapper.toResponse(any(Order.class))).thenReturn(testOrderResponse);
        
        List<OrderResponse> result = orderService.getAllOrders(OrderStatus.PENDING);
        
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(orderRepository, times(1)).findByStatus(OrderStatus.PENDING);
    }
    
    @Test
    void updateOrderStatus_Success() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);
        when(orderMapper.toResponse(testOrder)).thenReturn(testOrderResponse);
        
        OrderResponse result = orderService.updateOrderStatus(1L, OrderStatus.PROCESSING);
        
        assertNotNull(result);
        verify(orderRepository, times(1)).save(any(Order.class));
    }
    
    @Test
    void cancelOrder_Success() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);
        when(orderMapper.toResponse(testOrder)).thenReturn(testOrderResponse);
        
        OrderResponse result = orderService.cancelOrder(1L);
        
        assertNotNull(result);
        verify(orderRepository, times(1)).save(any(Order.class));
    }
    
    @Test
    void cancelOrder_InvalidStatus() {
        testOrder.setStatus(OrderStatus.SHIPPED);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        
        assertThrows(InvalidOrderOperationException.class, () -> orderService.cancelOrder(1L));
    }
    
    @Test
    void processPendingOrders_Success() {
        List<Order> pendingOrders = Arrays.asList(testOrder);
        when(orderRepository.findByStatus(OrderStatus.PENDING)).thenReturn(pendingOrders);
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);
        
        int result = orderService.processPendingOrders();
        
        assertEquals(1, result);
        verify(orderRepository, times(1)).findByStatus(OrderStatus.PENDING);
        verify(orderRepository, times(1)).save(any(Order.class));
    }
    
    // ============================================================
    // validateStatusTransition Test Cases - Complete Coverage
    // ============================================================
    
    @Test
    void updateOrderStatus_CannotUpdateCancelledOrder() {
        testOrder.setStatus(OrderStatus.CANCELLED);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        
        InvalidOrderOperationException exception = assertThrows(
            InvalidOrderOperationException.class,
            () -> orderService.updateOrderStatus(1L, OrderStatus.PROCESSING)
        );
        
        assertTrue(exception.getMessage().contains("Cannot update status of a cancelled order"));
        verify(orderRepository, never()).save(any(Order.class));
    }
    
    @Test
    void updateOrderStatus_CannotUpdateDeliveredOrder() {
        testOrder.setStatus(OrderStatus.DELIVERED);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        
        InvalidOrderOperationException exception = assertThrows(
            InvalidOrderOperationException.class,
            () -> orderService.updateOrderStatus(1L, OrderStatus.PROCESSING)
        );
        
        assertTrue(exception.getMessage().contains("Cannot update status of a delivered order"));
        verify(orderRepository, never()).save(any(Order.class));
    }
    
    @Test
    void updateOrderStatus_PendingToShipped_InvalidTransition() {
        testOrder.setStatus(OrderStatus.PENDING);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        
        InvalidOrderOperationException exception = assertThrows(
            InvalidOrderOperationException.class,
            () -> orderService.updateOrderStatus(1L, OrderStatus.SHIPPED)
        );
        
        assertTrue(exception.getMessage().contains("PENDING orders can only move to PROCESSING or be CANCELLED"));
        verify(orderRepository, never()).save(any(Order.class));
    }
    
    @Test
    void updateOrderStatus_ProcessingToPending_InvalidTransition() {
        testOrder.setStatus(OrderStatus.PROCESSING);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        
        InvalidOrderOperationException exception = assertThrows(
            InvalidOrderOperationException.class,
            () -> orderService.updateOrderStatus(1L, OrderStatus.PENDING)
        );
        
        assertTrue(exception.getMessage().contains("PROCESSING"));
        assertTrue(exception.getMessage().contains("SHIPPED"));
        verify(orderRepository, never()).save(any(Order.class));
    }
    
    @Test
    void updateOrderStatus_ProcessingToCancelled_InvalidTransition() {
        testOrder.setStatus(OrderStatus.PROCESSING);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        
        InvalidOrderOperationException exception = assertThrows(
            InvalidOrderOperationException.class,
            () -> orderService.updateOrderStatus(1L, OrderStatus.CANCELLED)
        );
        
        assertTrue(exception.getMessage().contains("PROCESSING"));
        verify(orderRepository, never()).save(any(Order.class));
    }
    
    @Test
    void updateOrderStatus_ProcessingToDelivered_InvalidTransition() {
        testOrder.setStatus(OrderStatus.PROCESSING);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        
        InvalidOrderOperationException exception = assertThrows(
            InvalidOrderOperationException.class,
            () -> orderService.updateOrderStatus(1L, OrderStatus.DELIVERED)
        );
        
        assertTrue(exception.getMessage().contains("PROCESSING"));
        verify(orderRepository, never()).save(any(Order.class));
    }
    
    @Test
    void updateOrderStatus_ShippedToPending_InvalidTransition() {
        testOrder.setStatus(OrderStatus.SHIPPED);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        
        InvalidOrderOperationException exception = assertThrows(
            InvalidOrderOperationException.class,
            () -> orderService.updateOrderStatus(1L, OrderStatus.PENDING)
        );
        
        assertTrue(exception.getMessage().contains("SHIPPED orders can only move to DELIVERED"));
        assertTrue(exception.getMessage().contains("SHIPPED"));
        assertTrue(exception.getMessage().contains("DELIVERED"));
        verify(orderRepository, never()).save(any(Order.class));
    }
    
    @Test
    void updateOrderStatus_ShippedToProcessing_InvalidTransition() {
        testOrder.setStatus(OrderStatus.SHIPPED);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        
        InvalidOrderOperationException exception = assertThrows(
            InvalidOrderOperationException.class,
            () -> orderService.updateOrderStatus(1L, OrderStatus.PROCESSING)
        );
        
        assertTrue(exception.getMessage().contains("SHIPPED"));
        verify(orderRepository, never()).save(any(Order.class));
    }
    
    @Test
    void updateOrderStatus_ShippedToCancelled_InvalidTransition() {
        testOrder.setStatus(OrderStatus.SHIPPED);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        
        InvalidOrderOperationException exception = assertThrows(
            InvalidOrderOperationException.class,
            () -> orderService.updateOrderStatus(1L, OrderStatus.CANCELLED)
        );
        
        assertTrue(exception.getMessage().contains("SHIPPED"));
        verify(orderRepository, never()).save(any(Order.class));
    }
    
    @Test
    void updateOrderStatus_PendingToProcessing_ValidTransition() {
        testOrder.setStatus(OrderStatus.PENDING);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);
        when(orderMapper.toResponse(testOrder)).thenReturn(testOrderResponse);
        
        OrderResponse result = orderService.updateOrderStatus(1L, OrderStatus.PROCESSING);
        
        assertNotNull(result);
        verify(orderRepository, times(1)).save(any(Order.class));
    }
    
    @Test
    void updateOrderStatus_PendingToCancelled_ValidTransition() {
        testOrder.setStatus(OrderStatus.PENDING);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);
        when(orderMapper.toResponse(testOrder)).thenReturn(testOrderResponse);
        
        OrderResponse result = orderService.updateOrderStatus(1L, OrderStatus.CANCELLED);
        
        assertNotNull(result);
        verify(orderRepository, times(1)).save(any(Order.class));
    }
    
    @Test
    void updateOrderStatus_ProcessingToShipped_ValidTransition() {
        testOrder.setStatus(OrderStatus.PROCESSING);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);
        when(orderMapper.toResponse(testOrder)).thenReturn(testOrderResponse);
        
        OrderResponse result = orderService.updateOrderStatus(1L, OrderStatus.SHIPPED);
        
        assertNotNull(result);
        verify(orderRepository, times(1)).save(any(Order.class));
    }
    
    @Test
    void updateOrderStatus_ShippedToDelivered_ValidTransition() {
        testOrder.setStatus(OrderStatus.SHIPPED);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);
        when(orderMapper.toResponse(testOrder)).thenReturn(testOrderResponse);
        
        OrderResponse result = orderService.updateOrderStatus(1L, OrderStatus.DELIVERED);
        
        assertNotNull(result);
        verify(orderRepository, times(1)).save(any(Order.class));
    }
}
