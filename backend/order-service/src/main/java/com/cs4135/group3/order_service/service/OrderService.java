package com.cs4135.group3.order_service.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cs4135.group3.order_service.events.OrderCreatedEvent;
import com.cs4135.group3.order_service.messaging.OrderCreatedRabbitPublisher;
import com.cs4135.group3.order_service.messaging.PaymentCompletedMessage;
import com.cs4135.group3.order_service.model.Order;
import com.cs4135.group3.order_service.model.OrderItem;
import com.cs4135.group3.order_service.model.OrderStatus;
import com.cs4135.group3.order_service.repository.OrderRepository;
import com.cs4135.group3.order_service.requests.OrderRequest;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final OrderCreatedRabbitPublisher orderCreatedRabbitPublisher;

    public Order createOrder(OrderRequest orderRequest)
    {
        // Build the parent order first so each mapped OrderItem can reference it.
        Order order = new Order();

        order.setUserId(orderRequest.userId());
        order.setOrderNumber(UUID.randomUUID().toString());
        order.setStatus(OrderStatus.PENDING);
        order.setOrderedDate(LocalDateTime.now());

        // Convert incoming request items into persistent order items linked back to this order.
        List<OrderItem> orderItems = orderRequest.items()
                .stream()
                .map(itemRequest -> {
                    OrderItem item = new OrderItem();
                    item.setProductId(itemRequest.productId());
                    item.setProductName(itemRequest.productName());
                    item.setPrice(itemRequest.price());
                    item.setQuantity(itemRequest.quantity());
                    item.setOrder(order);
                    return item;
                })
                .toList();

        order.setOrderItems(orderItems);

        // Total price is the sum of each item's price multiplied by its quantity.
        BigDecimal total = orderItems.stream()
                .map(item -> item.getPrice()
                        .multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        order.setTotalPrice(total);

        Order savedOrder = orderRepository.save(order);

        OrderCreatedEvent created = new OrderCreatedEvent(
                savedOrder.getId(),
                savedOrder.getUserId(),
                savedOrder.getTotalPrice());

        // In-process listeners (e.g. logging) stay local to this JVM.
        eventPublisher.publishEvent(created);

        // Async integration: payment-service will consume this from RabbitMQ (next steps).
        orderCreatedRabbitPublisher.publish(created);

        return savedOrder;
    }

    public List<Order> getOrdersByUserId(Long userId)
    {
        return orderRepository.findByUserId(userId);
    }

    public Order getOrderById(Long id)
    {
        return orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found"));
    }

    public Order updateOrderStatus(Long orderId, OrderStatus status)
    {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        order.setStatus(status);

        return orderRepository.save(order);
    }

    public Order cancelOrder(Long orderId)
    {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        order.setStatus(OrderStatus.CANCELLED);

        return orderRepository.save(order);
    }


    @Transactional
    public void applyPaymentResult(PaymentCompletedMessage msg) {
        Order order = orderRepository.findById(msg.orderId())
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (order.getStatus() != OrderStatus.PENDING) {
            log.debug("Ignoring payment completion for order {} in state {}", msg.orderId(), order.getStatus());
            return;
        }

        if (order.getTotalPrice().compareTo(msg.amount()) != 0) {
            log.warn("Payment amount {} does not match order total {} for order {}", msg.amount(), order.getTotalPrice(),
                    msg.orderId());
        }

        if ("SUCCESS".equalsIgnoreCase(msg.status())) {
            order.setStatus(OrderStatus.PAID);
        }
        else {
            order.setStatus(OrderStatus.CANCELLED);
        }

        orderRepository.save(order);
    }
}
