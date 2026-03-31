package com.cs4135.group3.order_service.service;

import com.cs4135.group3.order_service.model.Order;
import com.cs4135.group3.order_service.repository.OrderRepository;
import com.cs4135.group3.order_service.requests.OrderRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
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

        order.setUserId(orderRequest.userId());
        order.setOrderNumber(UUID.randomUUID().toString());
        order.setPrice(orderRequest.price());
        order.setProductName(orderRequest.productName());
        order.setQuantity(orderRequest.quantity());

        orderRepository.save(order);
    }

    public List<Order> getOrdersByUserId(Long userId)
    {
        return orderRepository.findByUserId(userId);
    }
}
