import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.server.ResponseStatusException;

import com.cs4135.group3.order_service.events.OrderCreatedEvent;
import com.cs4135.group3.order_service.integration.ProductStockClient;
import com.cs4135.group3.order_service.messaging.PaymentCompletedMessage;
import com.cs4135.group3.order_service.messaging.OrderCreatedRabbitPublisher;
import com.cs4135.group3.order_service.model.Order;
import com.cs4135.group3.order_service.model.OrderItem;
import com.cs4135.group3.order_service.model.OrderStatus;
import com.cs4135.group3.order_service.repository.OrderRepository;
import com.cs4135.group3.order_service.requests.AddOrderItemRequest;
import com.cs4135.group3.order_service.requests.OrderItemRequest;
import com.cs4135.group3.order_service.requests.OrderRequest;
import com.cs4135.group3.order_service.service.OrderService;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    private static final Authentication CUSTOMER_AUTH = auth("42", "ROLE_CUSTOMER");
    private static final Authentication OTHER_CUSTOMER_AUTH = auth("123", "ROLE_CUSTOMER");
    private static final Authentication ADMIN_AUTH = auth("7", "ROLE_ADMINISTRATOR");
    private static final UUID PRODUCT_ID_1 = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID PRODUCT_ID_2 = UUID.fromString("00000000-0000-0000-0000-000000000002");
    private static final UUID PRODUCT_ID_3 = UUID.fromString("00000000-0000-0000-0000-000000000003");
    private static final UUID PRODUCT_ID_5 = UUID.fromString("00000000-0000-0000-0000-000000000005");

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private OrderCreatedRabbitPublisher orderCreatedRabbitPublisher;

    @Mock
    private ProductStockClient productStockClient;

    @InjectMocks
    private OrderService orderService;

    private void stubSaveReturnsPersistedOrder() {
        doAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            order.setId(999L);
            return order;
        }).when(orderRepository).save(any(Order.class));
    }

    private static Authentication auth(String userId, String role) {
        return new UsernamePasswordAuthenticationToken(
                userId,
                null,
                List.of(new SimpleGrantedAuthority(role)));
    }

    @Test
    void createOrderMapsRequestAndSavesOrder() {
        stubSaveReturnsPersistedOrder();
        OrderRequest request = new OrderRequest(
                null,
                42L,
                null,
                List.of(new OrderItemRequest(PRODUCT_ID_1, "Mouse", new BigDecimal("49.99"), 3))
        );

        orderService.createOrder(request, CUSTOMER_AUTH);

        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).save(orderCaptor.capture());

        Order savedOrder = orderCaptor.getValue();
        assertEquals(42L, savedOrder.getUserId());
        assertEquals(999L, savedOrder.getId());
        assertNotNull(savedOrder.getOrderNumber());
        assertDoesNotThrow(() -> UUID.fromString(savedOrder.getOrderNumber()));
        assertEquals(1, savedOrder.getOrderItems().size());

        OrderItem item = savedOrder.getOrderItems().get(0);
        assertEquals("Mouse", item.getProductName());
        assertEquals(new BigDecimal("49.99"), item.getPrice());
        assertEquals(3, item.getQuantity());
        assertEquals(new BigDecimal("149.97"), savedOrder.getTotalPrice());
    }

    @Test
    void createOrderGeneratesNewOrderNumber() {
        stubSaveReturnsPersistedOrder();
        OrderRequest request = new OrderRequest(
                null,
                7L,
                "request-order-number",
                List.of(new OrderItemRequest(PRODUCT_ID_2, "Desk Lamp", new BigDecimal("15.50"), 1))
        );

        orderService.createOrder(request, ADMIN_AUTH);

        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).save(orderCaptor.capture());

        Order savedOrder = orderCaptor.getValue();
        assertNotNull(savedOrder.getOrderNumber());
        assertNotEquals("request-order-number", savedOrder.getOrderNumber());
        assertDoesNotThrow(() -> UUID.fromString(savedOrder.getOrderNumber()));
    }

    @Test
    void createOrderSavesExactlyOneOrder() {
        stubSaveReturnsPersistedOrder();
        OrderRequest request = new OrderRequest(
                null,
                11L,
                null,
                List.of(new OrderItemRequest(PRODUCT_ID_3, "Keyboard", new BigDecimal("120.00"), 2))
        );

        orderService.createOrder(request, auth("11", "ROLE_CUSTOMER"));

        verify(orderRepository).save(any(Order.class));
        verifyNoMoreInteractions(orderRepository);
    }

    @Test
    void createOrderWithMultipleItemsCalculatesTotalCorrectly() {
        stubSaveReturnsPersistedOrder();
        OrderRequest request = new OrderRequest(
                null,
                42L,
                null,
                List.of(
                        new OrderItemRequest(PRODUCT_ID_1, "Mouse", new BigDecimal("20.00"), 2),
                        new OrderItemRequest(PRODUCT_ID_2, "Keyboard", new BigDecimal("50.00"), 1))
        );

        orderService.createOrder(request, CUSTOMER_AUTH);

        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).save(orderCaptor.capture());

        Order savedOrder = orderCaptor.getValue();
        assertEquals(2, savedOrder.getOrderItems().size());
        assertEquals(new BigDecimal("90.00"), savedOrder.getTotalPrice());
    }

    @Test
    void createOrderSetsPendingStatusAndOrderedDate() {
        stubSaveReturnsPersistedOrder();
        OrderRequest request = new OrderRequest(
                null,
                12L,
                null,
                List.of(new OrderItemRequest(PRODUCT_ID_1, "Headphones", new BigDecimal("35.00"), 1))
        );

        orderService.createOrder(request, auth("12", "ROLE_CUSTOMER"));

        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).save(orderCaptor.capture());

        Order savedOrder = orderCaptor.getValue();
        assertEquals(OrderStatus.PENDING, savedOrder.getStatus());
        assertNotNull(savedOrder.getOrderedDate());
        assertTrue(savedOrder.getOrderedDate().isBefore(LocalDateTime.now().plusSeconds(1)));
    }

    @Test
    void createOrderLinksEachOrderItemBackToParentOrder() {
        stubSaveReturnsPersistedOrder();
        OrderRequest request = new OrderRequest(
                null,
                21L,
                null,
                List.of(
                        new OrderItemRequest(PRODUCT_ID_1, "Mouse", new BigDecimal("20.00"), 1),
                        new OrderItemRequest(PRODUCT_ID_2, "Keyboard", new BigDecimal("45.00"), 1))
        );

        orderService.createOrder(request, auth("21", "ROLE_CUSTOMER"));

        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).save(orderCaptor.capture());

        Order savedOrder = orderCaptor.getValue();
        assertTrue(savedOrder.getOrderItems().stream().allMatch(item -> item.getOrder() == savedOrder));
    }

    @Test
    void createOrderPublishesOrderCreatedEvent() {
        stubSaveReturnsPersistedOrder();
        OrderRequest request = new OrderRequest(
                null,
                33L,
                null,
                List.of(new OrderItemRequest(PRODUCT_ID_5, "Webcam", new BigDecimal("80.00"), 2))
        );

        orderService.createOrder(request, auth("33", "ROLE_CUSTOMER"));

        ArgumentCaptor<OrderCreatedEvent> eventCaptor = ArgumentCaptor.forClass(OrderCreatedEvent.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());

        OrderCreatedEvent event = eventCaptor.getValue();
        assertEquals(999L, event.orderId());
        assertEquals(33L, event.userId());
        assertEquals(new BigDecimal("160.00"), event.totalAmount());
        verify(orderCreatedRabbitPublisher).publish(event);
    }

    @Test
    void createOrderWithEmptyItemsSetsZeroTotal() {
        stubSaveReturnsPersistedOrder();
        OrderRequest request = new OrderRequest(null, 55L, null, List.of());

        Order savedOrder = orderService.createOrder(request, auth("55", "ROLE_CUSTOMER"));

        assertNotNull(savedOrder);
        assertTrue(savedOrder.getOrderItems().isEmpty());
        assertEquals(BigDecimal.ZERO, savedOrder.getTotalPrice());
        verify(orderRepository).save(any(Order.class));
        verify(eventPublisher).publishEvent(any(OrderCreatedEvent.class));
    }

    @Test
    void createOrderThrowsWhenItemsAreNull() {
        OrderRequest request = new OrderRequest(null, 66L, null, null);

        assertThrows(NullPointerException.class, () -> orderService.createOrder(request, auth("66", "ROLE_CUSTOMER")));
    }

    @Test
    void createOrderRejectsMismatchedUserIdInRequest() {
        OrderRequest request = new OrderRequest(
                null,
                999L,
                null,
                List.of(new OrderItemRequest(PRODUCT_ID_1, "Mouse", new BigDecimal("49.99"), 1))
        );

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> orderService.createOrder(request, CUSTOMER_AUTH));

        assertEquals(403, exception.getStatusCode().value());
        assertEquals("You can only create orders for yourself", exception.getReason());
    }

    @Test
    void getOrdersByUserIdReturnsOnlyOrdersForRequestedUser() {
        Order firstOrder = new Order();
        firstOrder.setId(1L);
        firstOrder.setUserId(42L);

        Order secondOrder = new Order();
        secondOrder.setId(2L);
        secondOrder.setUserId(42L);

        when(orderRepository.findByUserId(42L)).thenReturn(List.of(firstOrder, secondOrder));

        List<Order> userOrders = orderService.getOrdersByUserId(42L, CUSTOMER_AUTH);

        assertEquals(2, userOrders.size());
        assertTrue(userOrders.stream().allMatch(order -> order.getUserId().equals(42L)));
        verify(orderRepository).findByUserId(42L);
    }

    @Test
    void getOrdersByUserIdRejectsOtherCustomer() {
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> orderService.getOrdersByUserId(42L, OTHER_CUSTOMER_AUTH));

        assertEquals(403, exception.getStatusCode().value());
        assertEquals("You can only view your own orders", exception.getReason());
    }

    @Test
    void getOrdersByUserIdAllowsAdmin() {
        when(orderRepository.findByUserId(42L)).thenReturn(List.of());

        orderService.getOrdersByUserId(42L, ADMIN_AUTH);

        verify(orderRepository).findByUserId(42L);
    }

    @Test
    void getOrderByIdReturnsOrderWhenFound() {
        Order order = new Order();
        order.setId(7L);
        order.setUserId(42L);
        when(orderRepository.findById(7L)).thenReturn(Optional.of(order));

        Order result = orderService.getOrderById(7L, CUSTOMER_AUTH);

        assertEquals(7L, result.getId());
        assertEquals(42L, result.getUserId());
        verify(orderRepository).findById(7L);
    }

    @Test
    void getOrderByIdRejectsOtherCustomer() {
        Order order = new Order();
        order.setId(7L);
        order.setUserId(42L);
        when(orderRepository.findById(7L)).thenReturn(Optional.of(order));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> orderService.getOrderById(7L, OTHER_CUSTOMER_AUTH));

        assertEquals(403, exception.getStatusCode().value());
        assertEquals("You can only view your own orders", exception.getReason());
    }

    @Test
    void getOrderByIdThrowsWhenOrderDoesNotExist() {
        when(orderRepository.findById(404L)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> orderService.getOrderById(404L, CUSTOMER_AUTH));
        assertEquals(404, exception.getStatusCode().value());
        assertEquals("Order not found", exception.getReason());
        verify(orderRepository).findById(404L);
    }

    @Test
    void updateOrderStatusUpdatesAndSavesOrder() {
        Order existingOrder = new Order();
        existingOrder.setId(10L);
        existingOrder.setStatus(OrderStatus.PENDING);
        when(orderRepository.findById(10L)).thenReturn(Optional.of(existingOrder));
        when(orderRepository.save(existingOrder)).thenReturn(existingOrder);

        Order updatedOrder = orderService.updateOrderStatus(10L, OrderStatus.PAID, ADMIN_AUTH);

        assertEquals(OrderStatus.PAID, updatedOrder.getStatus());
        verify(orderRepository).findById(10L);
        verify(orderRepository).save(existingOrder);
    }

    @Test
    void updateOrderStatusRejectsNonAdmin() {
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> orderService.updateOrderStatus(10L, OrderStatus.PAID, CUSTOMER_AUTH));

        assertEquals(403, exception.getStatusCode().value());
        assertEquals("Administrator role required", exception.getReason());
    }

    @Test
    void updateOrderStatusThrowsWhenOrderDoesNotExist() {
        when(orderRepository.findById(999L)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> orderService.updateOrderStatus(999L, OrderStatus.PAID, ADMIN_AUTH));
        assertEquals(404, exception.getStatusCode().value());
        assertEquals("Order not found", exception.getReason());
        verify(orderRepository).findById(999L);
    }

    @Test
    void cancelOrderSetsCancelledStatusAndSavesOrder() {
        Order existingOrder = new Order();
        existingOrder.setId(20L);
        existingOrder.setUserId(42L);
        existingOrder.setStatus(OrderStatus.PENDING);
        when(orderRepository.findById(20L)).thenReturn(Optional.of(existingOrder));
        when(orderRepository.save(existingOrder)).thenReturn(existingOrder);

        Order cancelledOrder = orderService.cancelOrder(20L, CUSTOMER_AUTH);

        assertEquals(OrderStatus.CANCELLED, cancelledOrder.getStatus());
        verify(orderRepository).findById(20L);
        verify(orderRepository).save(existingOrder);
    }

    @Test
    void cancelOrderAllowsAdmin() {
        Order existingOrder = new Order();
        existingOrder.setId(20L);
        existingOrder.setUserId(42L);
        existingOrder.setStatus(OrderStatus.PENDING);
        when(orderRepository.findById(20L)).thenReturn(Optional.of(existingOrder));
        when(orderRepository.save(existingOrder)).thenReturn(existingOrder);

        Order cancelledOrder = orderService.cancelOrder(20L, ADMIN_AUTH);

        assertEquals(OrderStatus.CANCELLED, cancelledOrder.getStatus());
        verify(orderRepository).save(existingOrder);
    }

    @Test
    void cancelOrderRejectsOtherCustomer() {
        Order existingOrder = new Order();
        existingOrder.setId(20L);
        existingOrder.setUserId(42L);
        when(orderRepository.findById(20L)).thenReturn(Optional.of(existingOrder));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> orderService.cancelOrder(20L, OTHER_CUSTOMER_AUTH));

        assertEquals(403, exception.getStatusCode().value());
        assertEquals("You can only cancel your own orders", exception.getReason());
    }

    @Test
    void cancelOrderThrowsWhenOrderDoesNotExist() {
        when(orderRepository.findById(888L)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> orderService.cancelOrder(888L, CUSTOMER_AUTH));
        assertEquals(404, exception.getStatusCode().value());
        assertEquals("Order not found", exception.getReason());
        verify(orderRepository).findById(888L);
    }

    @Test
    void addItemAddsLineAndRecalculatesTotal() {
        Order order = new Order();
        order.setId(30L);
        order.setUserId(42L);
        order.setStatus(OrderStatus.PENDING);
        order.setOrderItems(new java.util.ArrayList<>(List.of(
                new OrderItem(1L, PRODUCT_ID_1, "Mouse", new BigDecimal("10.00"), 2, order))));
        order.setTotalPrice(new BigDecimal("20.00"));
        when(orderRepository.findById(30L)).thenReturn(Optional.of(order));
        when(orderRepository.save(order)).thenReturn(order);

        Order updated = orderService.addItem(
                30L,
                new AddOrderItemRequest(PRODUCT_ID_2, "Keyboard", new BigDecimal("15.00"), 1),
                CUSTOMER_AUTH);

        assertEquals(2, updated.getOrderItems().size());
        assertEquals(new BigDecimal("35.00"), updated.getTotalPrice());
        assertEquals(order, updated.getOrderItems().get(1).getOrder());
        verify(orderRepository).save(order);
    }

    @Test
    void addItemRejectsNonPendingOrders() {
        Order order = new Order();
        order.setId(30L);
        order.setUserId(42L);
        order.setStatus(OrderStatus.PAID);
        order.setOrderItems(new java.util.ArrayList<>());
        when(orderRepository.findById(30L)).thenReturn(Optional.of(order));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> orderService.addItem(
                        30L,
                        new AddOrderItemRequest(PRODUCT_ID_2, "Keyboard", new BigDecimal("15.00"), 1),
                        CUSTOMER_AUTH));

        assertEquals(403, exception.getStatusCode().value());
        assertEquals("Only pending orders can be modified", exception.getReason());
    }

    @Test
    void removeItemDeletesLineAndRecalculatesTotal() {
        Order order = new Order();
        order.setId(31L);
        order.setUserId(42L);
        order.setStatus(OrderStatus.PENDING);
        OrderItem first = new OrderItem(1L, PRODUCT_ID_1, "Mouse", new BigDecimal("10.00"), 2, order);
        OrderItem second = new OrderItem(2L, PRODUCT_ID_2, "Keyboard", new BigDecimal("15.00"), 1, order);
        order.setOrderItems(new java.util.ArrayList<>(List.of(first, second)));
        order.setTotalPrice(new BigDecimal("35.00"));
        when(orderRepository.findById(31L)).thenReturn(Optional.of(order));
        when(orderRepository.save(order)).thenReturn(order);

        Order updated = orderService.removeItem(31L, 2L, CUSTOMER_AUTH);

        assertEquals(1, updated.getOrderItems().size());
        assertEquals(1L, updated.getOrderItems().get(0).getId());
        assertEquals(new BigDecimal("20.00"), updated.getTotalPrice());
        verify(orderRepository).save(order);
    }

    @Test
    void increaseItemQuantityUpdatesQuantityAndTotal() {
        Order order = new Order();
        order.setId(32L);
        order.setUserId(42L);
        order.setStatus(OrderStatus.PENDING);
        OrderItem item = new OrderItem(1L, PRODUCT_ID_1, "Mouse", new BigDecimal("10.00"), 2, order);
        order.setOrderItems(new java.util.ArrayList<>(List.of(item)));
        order.setTotalPrice(new BigDecimal("20.00"));
        when(orderRepository.findById(32L)).thenReturn(Optional.of(order));
        when(orderRepository.save(order)).thenReturn(order);

        Order updated = orderService.increaseItemQuantity(32L, 1L, 3, CUSTOMER_AUTH);

        assertEquals(5, updated.getOrderItems().get(0).getQuantity());
        assertEquals(new BigDecimal("50.00"), updated.getTotalPrice());
        verify(orderRepository).save(order);
    }

    @Test
    void decreaseItemQuantityUpdatesQuantityAndTotal() {
        Order order = new Order();
        order.setId(33L);
        order.setUserId(42L);
        order.setStatus(OrderStatus.PENDING);
        OrderItem item = new OrderItem(1L, PRODUCT_ID_1, "Mouse", new BigDecimal("10.00"), 4, order);
        order.setOrderItems(new java.util.ArrayList<>(List.of(item)));
        order.setTotalPrice(new BigDecimal("40.00"));
        when(orderRepository.findById(33L)).thenReturn(Optional.of(order));
        when(orderRepository.save(order)).thenReturn(order);

        Order updated = orderService.decreaseItemQuantity(33L, 1L, 2, CUSTOMER_AUTH);

        assertEquals(2, updated.getOrderItems().get(0).getQuantity());
        assertEquals(new BigDecimal("20.00"), updated.getTotalPrice());
        verify(orderRepository).save(order);
    }

    @Test
    void decreaseItemQuantityRejectsReducingBelowOne() {
        Order order = new Order();
        order.setId(34L);
        order.setUserId(42L);
        order.setStatus(OrderStatus.PENDING);
        OrderItem item = new OrderItem(1L, PRODUCT_ID_1, "Mouse", new BigDecimal("10.00"), 1, order);
        order.setOrderItems(new java.util.ArrayList<>(List.of(item)));
        when(orderRepository.findById(34L)).thenReturn(Optional.of(order));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> orderService.decreaseItemQuantity(34L, 1L, 1, CUSTOMER_AUTH));

        assertEquals(403, exception.getStatusCode().value());
        assertEquals("Quantity cannot be reduced below 1", exception.getReason());
    }

    @Test
    void modifyOrderRejectsOtherCustomer() {
        Order order = new Order();
        order.setId(35L);
        order.setUserId(42L);
        order.setStatus(OrderStatus.PENDING);
        order.setOrderItems(new java.util.ArrayList<>());
        when(orderRepository.findById(35L)).thenReturn(Optional.of(order));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> orderService.addItem(
                        35L,
                        new AddOrderItemRequest(PRODUCT_ID_2, "Keyboard", new BigDecimal("15.00"), 1),
                        OTHER_CUSTOMER_AUTH));

        assertEquals(403, exception.getStatusCode().value());
        assertEquals("You can only modify your own orders", exception.getReason());
    }

    @Test
    void applyPaymentResultDeductsStockBeforeMarkingOrderPaid() {
        Order order = new Order();
        order.setId(10L);
        order.setStatus(OrderStatus.PENDING);
        order.setTotalPrice(new BigDecimal("99.99"));
        order.setOrderItems(List.of(new OrderItem(null, PRODUCT_ID_1, "Mouse", new BigDecimal("99.99"), 1, order)));
        when(orderRepository.findById(10L)).thenReturn(Optional.of(order));
        when(orderRepository.save(order)).thenReturn(order);

        orderService.applyPaymentResult(new PaymentCompletedMessage(UUID.randomUUID(), 10L, 42L, new BigDecimal("99.99"), "SUCCESS"));

        verify(productStockClient).deductStock(order.getOrderItems());
        assertEquals(OrderStatus.PAID, order.getStatus());
        verify(orderRepository).save(order);
    }

    @Test
    void applyPaymentResultDoesNotMarkOrderPaidWhenStockUpdateFails() {
        Order order = new Order();
        order.setId(10L);
        order.setStatus(OrderStatus.PENDING);
        order.setTotalPrice(new BigDecimal("99.99"));
        order.setOrderItems(List.of(new OrderItem(null, PRODUCT_ID_1, "Mouse", new BigDecimal("99.99"), 1, order)));
        when(orderRepository.findById(10L)).thenReturn(Optional.of(order));
        org.mockito.Mockito.doThrow(new RuntimeException("stock failed"))
                .when(productStockClient)
                .deductStock(order.getOrderItems());

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> orderService.applyPaymentResult(new PaymentCompletedMessage(
                        UUID.randomUUID(),
                        10L,
                        42L,
                        new BigDecimal("99.99"),
                        "SUCCESS")));

        assertEquals("stock failed", exception.getMessage());
        assertEquals(OrderStatus.PENDING, order.getStatus());
        verify(productStockClient).deductStock(order.getOrderItems());
        verify(orderRepository).findById(10L);
        verify(orderRepository, org.mockito.Mockito.never()).save(any(Order.class));
    }
}
