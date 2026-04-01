package com.cs4135.group3.order_service.controller;

import com.cs4135.group3.order_service.model.Order;
import com.cs4135.group3.order_service.requests.OrderRequest;
import com.cs4135.group3.order_service.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/order")
@RequiredArgsConstructor
public class OrderController
{
    // Delegates HTTP requests to the service layer so the controller stays thin.
    private final OrderService orderService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Order createOrder(@RequestBody OrderRequest orderRequest)
    {
        return orderService.createOrder(orderRequest);
    }

    @GetMapping("/user/{userId}")
    public List<Order> getOrdersByUserId(@PathVariable Long userId)
    {
        // Returns all orders that belong to a single user-service account.
        return orderService.getOrdersByUserId(userId);
    }

    @GetMapping("/{id}")
    public Order getOrderById(@PathVariable Long id)
    {
        return orderService.getOrderById(id);
    }
}
