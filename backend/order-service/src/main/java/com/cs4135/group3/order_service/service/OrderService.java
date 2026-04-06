package com.cs4135.group3.order_service.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import org.springframework.context.ApplicationEventPublisher;
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
import com.cs4135.group3.order_service.requests.OrderItemRequest;
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
                .map(itemRequest -> toOrderItem(order, itemRequest))
                .toList();

        order.setOrderItems(orderItems);
        recalculateTotal(order);

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

    public Order addItem(Long orderId, OrderItemRequest itemRequest, Authentication authentication) {
        Order order = getMutableOrder(orderId, authentication);
        List<OrderItem> items = order.getOrderItems() == null ? new ArrayList<>() : new ArrayList<>(order.getOrderItems());
        // Work on a mutable copy so JPA can detect both additions and removals when we replace the collection.
        items.add(toOrderItem(order, itemRequest));
        order.setOrderItems(items);
        recalculateTotal(order);
        return orderRepository.save(order);
    }

    public Order removeItem(Long orderId, Long itemId, Authentication authentication) {
        Order order = getMutableOrder(orderId, authentication);
        List<OrderItem> items = order.getOrderItems() == null ? new ArrayList<>() : new ArrayList<>(order.getOrderItems());
        boolean removed = items.removeIf(item -> itemId.equals(item.getId()));
        if (!removed) {
            throw new ResponseStatusException(NOT_FOUND, "Order item not found");
        }

        order.setOrderItems(items);
        recalculateTotal(order);
        return orderRepository.save(order);
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
            // Only deduct inventory once payment has actually succeeded.
            productStockClient.deductStock(order);
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

    private Order getMutableOrder(Long orderId, Authentication authentication) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Order not found"));
        enforceOwnership(order, authentication, "You can only modify your own orders");
        // Once payment has progressed beyond pending, line items must stop changing.
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new ResponseStatusException(FORBIDDEN, "Only pending orders can be modified");
        }
        return order;
    }

    private OrderItem toOrderItem(Order order, OrderItemRequest itemRequest) {
        OrderItem item = new OrderItem();
        item.setProductId(itemRequest.productId());
        item.setProductName(itemRequest.productName());
        item.setPrice(itemRequest.price());
        item.setQuantity(itemRequest.quantity());
        item.setOrder(order);
        return item;
    }

    private void recalculateTotal(Order order) {
        // Keep the stored order total aligned with the current set of order items.
        BigDecimal total = order.getOrderItems().stream()
                .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        order.setTotalPrice(total);
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
