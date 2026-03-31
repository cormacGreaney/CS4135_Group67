package com.cs4135.group3.order_service.controller;

import com.cs4135.group3.order_service.requests.OrderRequest;
import com.cs4135.group3.order_service.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/order")
@RequiredArgsConstructor
public class OrderController
{

    private final OrderService orderService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public String createOrder(@RequestBody OrderRequest orderRequest)
    {
        orderService.createOrder(orderRequest);
        return "Order Created";
    }
}
