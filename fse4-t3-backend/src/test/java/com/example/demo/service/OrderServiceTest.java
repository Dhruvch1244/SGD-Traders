package com.example.demo.service;

import com.example.demo.models.Order;
import com.example.demo.repository.OrderRepository;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Arrays;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {
    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private OrderService orderService;

    @Test
    void getAllOrders_returnsOrderList() {
        Order order1 = new Order();
        Order order2 = new Order();
        List<Order> orders = Arrays.asList(order1, order2);
        when(orderRepository.findAll()).thenReturn(orders);
        List<Order> result = orderService.getAllOrders();
        assertEquals(2, result.size());
        assertSame(order1, result.get(0));
        assertSame(order2, result.get(1));
    }

    @Test
    void getAllOrders_returnsEmptyList() {
        when(orderRepository.findAll()).thenReturn(Arrays.asList());
        List<Order> result = orderService.getAllOrders();
        assertTrue(result.isEmpty());
    }
}
