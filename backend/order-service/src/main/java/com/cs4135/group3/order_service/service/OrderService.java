package com.cs4135.group3.order_service.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.context.ApplicationEventPublisher;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.cs4135.group3.order_service.events.OrderCreatedEvent;
import com.cs4135.group3.order_service.integration.ProductStockClient;
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
    private final ProductStockClient productStockClient;

    public Order createOrder(OrderRequest orderRequest, Authentication authentication)
    {
        // Build the parent order first so each mapped OrderItem can reference it.
        Order order = new Order();
        Long userId = parseUserId(authentication);

        // Ignore spoofed ownership by forcing the persisted user id to match the JWT subject.
        if (orderRequest.userId() != null && !orderRequest.userId().equals(userId)) {
            throw new ResponseStatusException(FORBIDDEN, "You can only create orders for yourself");
        }

        order.setUserId(userId);
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

    public List<Order> getOrdersByUserId(Long userId, Authentication authentication)
    {
        enforceSameUserOrAdmin(userId, authentication, "You can only view your own orders");
        return orderRepository.findByUserId(userId);
    }

    public Order getOrderById(Long id, Authentication authentication)
    {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Order not found"));
        enforceOwnership(order, authentication, "You can only view your own orders");
        return order;
    }

    public Order updateOrderStatus(Long orderId, OrderStatus status, Authentication authentication)
    {
        enforceAdmin(authentication);
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Order not found"));

        order.setStatus(status);

        return orderRepository.save(order);
    }

    public Order cancelOrder(Long orderId, Authentication authentication)
    {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Order not found"));
        enforceOwnership(order, authentication, "You can only cancel your own orders");

        order.setStatus(OrderStatus.CANCELLED);

        return orderRepository.save(order);
    }


    @Transactional
    public void applyPaymentResult(PaymentCompletedMessage msg) {
        Order order = orderRepository.findById(msg.orderId()).orElse(null);
        if (order == null) {
            // Do not throw here, this stops stale/poisoned messages that are re-delivered forever by the listener.
            log.warn("Ignoring PaymentCompleted for unknown orderId={}", msg.orderId());
            return;
        }

        if (order.getStatus() != OrderStatus.PENDING) {
            log.debug("Ignoring payment completion for order {} in state {}", msg.orderId(), order.getStatus());
            return;
        }

        if (order.getTotalPrice().compareTo(msg.amount()) != 0) {
            log.warn("Payment amount {} does not match order total {} for order {}", msg.amount(), order.getTotalPrice(),
                    msg.orderId());
        }

        if ("SUCCESS".equalsIgnoreCase(msg.status())) {
            // Stock must be deducted first; if that call fails, the transaction leaves the order in PENDING.
            productStockClient.deductStock(order.getOrderItems());
            order.setStatus(OrderStatus.PAID);
        }
        else {
            order.setStatus(OrderStatus.CANCELLED);
        }

        orderRepository.save(order);
    }

    private void enforceOwnership(Order order, Authentication authentication, String message) {
        enforceSameUserOrAdmin(order.getUserId(), authentication, message);
    }

    private void enforceSameUserOrAdmin(Long userId, Authentication authentication, String message) {
        if (isAdmin(authentication)) {
            return;
        }

        // Regular users are restricted to resources owned by the JWT subject.
        if (!userId.equals(parseUserId(authentication))) {
            throw new ResponseStatusException(FORBIDDEN, message);
        }
    }

    private void enforceAdmin(Authentication authentication) {
        if (!isAdmin(authentication)) {
            throw new ResponseStatusException(FORBIDDEN, "Administrator role required");
        }
    }

    private Long parseUserId(Authentication authentication) {
        try {
            return Long.valueOf(authentication.getName());
        }
        catch (NumberFormatException ex) {
            throw new ResponseStatusException(FORBIDDEN, "Invalid authenticated user");
        }
    }

    private boolean isAdmin(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch("ROLE_ADMINISTRATOR"::equals);
    }
}
