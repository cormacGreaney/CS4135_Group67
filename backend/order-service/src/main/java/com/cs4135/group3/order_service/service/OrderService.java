package com.cs4135.group3.order_service.service;


import com.cs4135.group3.order_service.model.Order;
import com.cs4135.group3.order_service.repository.OrderRepository;
import com.cs4135.group3.order_service.requests.OrderRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderService 
{
    private final OrderRepository orderRepository;

    public void createOrder(OrderRequest orderRequest) 
    {
        // Logic to create an order
        Order order = new Order();
        order.setOrderId(UUID.randomUUID().toString());
        order.setUserId(order.getUserId());
        order.setTotalPrice(orderRequest.totalPrice().doubleValue());
        order.setOrderStatus(orderRequest.orderStatus());
        order.setOrderDate(orderRequest.orderDate());

        orderRepository.save(order);
    }
}
