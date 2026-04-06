package com.cs4135.group3.order_service.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.cs4135.group3.order_service.model.OrderStatus;
import com.cs4135.group3.order_service.requests.OrderItemRequest;
import com.cs4135.group3.order_service.requests.OrderRequest;
import com.cs4135.group3.order_service.service.OrderService;
import com.cs4135.group3.order_service.web.dto.OrderResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/order")
@RequiredArgsConstructor
public class OrderController
{
    // Delegates HTTP requests to the service layer so the controller stays thin.
    private final OrderService orderService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrderResponse createOrder(@RequestBody OrderRequest orderRequest, Authentication authentication)
    {
        return OrderResponse.fromEntity(orderService.createOrder(orderRequest, authentication));
    }

    @PostMapping("/{orderId}/items")
    public OrderResponse addItem(
            @PathVariable Long orderId,
            @RequestBody OrderItemRequest itemRequest,
            Authentication authentication)
    {
        // Lets a customer adjust basket contents before the order leaves PENDING.
        return OrderResponse.fromEntity(orderService.addItem(orderId, itemRequest, authentication));
    }

    @GetMapping("/user/{userId}")
    public List<OrderResponse> getOrdersByUserId(@PathVariable Long userId, Authentication authentication)
    {
        // Returns all orders that belong to a single user-service account.
        return orderService.getOrdersByUserId(userId, authentication).stream().map(OrderResponse::fromEntity).toList();
    }

    @GetMapping("/{id}")
    public OrderResponse getOrderById(@PathVariable Long id, Authentication authentication)
    {
        return OrderResponse.fromEntity(orderService.getOrderById(id, authentication));
    }

    @PutMapping("/{orderId}/status")
    public OrderResponse updateStatus(
            @PathVariable Long orderId,
            @RequestParam OrderStatus status,
            Authentication authentication)
    {
        return OrderResponse.fromEntity(orderService.updateOrderStatus(orderId, status, authentication));
    }

    @PutMapping("/{orderId}/cancel")
    public OrderResponse cancelOrder(@PathVariable Long orderId, Authentication authentication)
    {
        return OrderResponse.fromEntity(orderService.cancelOrder(orderId, authentication));
    }

    @DeleteMapping("/{orderId}/items/{itemId}")
    public OrderResponse removeItem(
            @PathVariable Long orderId,
            @PathVariable Long itemId,
            Authentication authentication)
    {
        // Removes one existing order line and returns the updated order snapshot.
        return OrderResponse.fromEntity(orderService.removeItem(orderId, itemId, authentication));
    }
}
