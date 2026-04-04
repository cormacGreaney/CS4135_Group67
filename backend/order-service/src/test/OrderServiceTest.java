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

import com.cs4135.group3.order_service.events.OrderCreatedEvent;
import com.cs4135.group3.order_service.messaging.OrderCreatedRabbitPublisher;
import com.cs4135.group3.order_service.model.Order;
import com.cs4135.group3.order_service.model.OrderItem;
import com.cs4135.group3.order_service.model.OrderStatus;
import com.cs4135.group3.order_service.repository.OrderRepository;
import com.cs4135.group3.order_service.requests.OrderItemRequest;
import com.cs4135.group3.order_service.requests.OrderRequest;
import com.cs4135.group3.order_service.service.OrderService;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private OrderCreatedRabbitPublisher orderCreatedRabbitPublisher;

    @InjectMocks
    private OrderService orderService;

    private void stubSaveReturnsPersistedOrder() {
        doAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            order.setId(999L);
            return order;
        }).when(orderRepository).save(any(Order.class));
    }

    @Test
    void createOrderMapsRequestAndSavesOrder() {
        // Arrange a single-item request so we can verify user mapping, item mapping, and total calculation.
        stubSaveReturnsPersistedOrder();
        OrderRequest request = new OrderRequest(
                null,
                42L,
                null,
                List.of(
                        new OrderItemRequest(
                                1L,
                                "Mouse",
                                new BigDecimal("49.99"),
                                3
                        )
                )
        );

        // Act by creating the order through the service.
        orderService.createOrder(request);

        // Capture the Order passed to the repository so we can inspect the saved state.
        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).save(orderCaptor.capture());

        Order savedOrder = orderCaptor.getValue();

        // The service should copy the user id and leave the request id unset before persistence.
        assertEquals(42L, savedOrder.getUserId());
        assertEquals(999L, savedOrder.getId());

        // The service should always generate a new UUID order number.
        assertNotNull(savedOrder.getOrderNumber());
        assertDoesNotThrow(() -> UUID.fromString(savedOrder.getOrderNumber()));

        // The request's items should be converted into persistent OrderItem objects.
        assertEquals(1, savedOrder.getOrderItems().size());

        OrderItem item = savedOrder.getOrderItems().get(0);

        assertEquals("Mouse", item.getProductName());
        assertEquals(new BigDecimal("49.99"), item.getPrice());
        assertEquals(3, item.getQuantity());

        // Total price should equal item price multiplied by quantity.
        assertEquals(new BigDecimal("149.97"), savedOrder.getTotalPrice());
    }

    @Test
    void createOrderGeneratesNewOrderNumber() {
        // Arrange a request with an existing order number to confirm the service replaces it.
        stubSaveReturnsPersistedOrder();
        OrderRequest request = new OrderRequest(
                null,
                77L,
                "request-order-number",
                List.of(
                        new OrderItemRequest(
                                2L,
                                "Desk Lamp",
                                new BigDecimal("15.50"),
                                1
                        )
                )
        );

        // Act by creating the order.
        orderService.createOrder(request);

        // Capture the saved order and verify the generated order number is still a UUID.
        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).save(orderCaptor.capture());

        Order savedOrder = orderCaptor.getValue();

        // The service should not rely on any client-supplied order number.
        assertNotNull(savedOrder.getOrderNumber());
        assertNotEquals("request-order-number", savedOrder.getOrderNumber());
        assertDoesNotThrow(() -> UUID.fromString(savedOrder.getOrderNumber()));
    }

    @Test
    void createOrderSavesExactlyOneOrder() {
        // Arrange a valid request and check that the service performs only one repository save.
        stubSaveReturnsPersistedOrder();
        OrderRequest request = new OrderRequest(
                null,
                11L,
                null,
                List.of(
                        new OrderItemRequest(
                                3L,
                                "Keyboard",
                                new BigDecimal("120.00"),
                                2
                        )
                )
        );

        // Act by creating the order once.
        orderService.createOrder(request);

        // Only one save should occur and there should be no extra repository calls.
        verify(orderRepository).save(org.mockito.ArgumentMatchers.any(Order.class));
        verifyNoMoreInteractions(orderRepository);
    }

    @Test
    void createOrderWithMultipleItemsCalculatesTotalCorrectly() {
        // Arrange multiple items so the total must be calculated across more than one line item.
        stubSaveReturnsPersistedOrder();
        OrderRequest request = new OrderRequest(
                null,
                42L,
                null,
                List.of(
                        new OrderItemRequest(
                                1L,
                                "Mouse",
                                new BigDecimal("20.00"),
                                2
                        ),
                        new OrderItemRequest(
                                2L,
                                "Keyboard",
                                new BigDecimal("50.00"),
                                1
                        )
                )
        );

        // Act by creating the order.
        orderService.createOrder(request);

        // Capture the saved order so we can inspect the generated order items and final total.
        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).save(orderCaptor.capture());

        Order savedOrder = orderCaptor.getValue();

        // Both items should be preserved on the order.
        assertEquals(2, savedOrder.getOrderItems().size());

        // Total should equal (20.00 * 2) + (50.00 * 1).
        assertEquals(
                new BigDecimal("90.00"),
                savedOrder.getTotalPrice()
        );
    }

    @Test
    void createOrderSetsPendingStatusAndOrderedDate() {
        // Arrange a valid request and capture the saved order metadata.
        stubSaveReturnsPersistedOrder();
        OrderRequest request = new OrderRequest(
                null,
                12L,
                null,
                List.of(new OrderItemRequest(1L, "Headphones", new BigDecimal("35.00"), 1))
        );

        // Act by creating the order.
        orderService.createOrder(request);

        // The service should mark new orders as pending and timestamp them immediately.
        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).save(orderCaptor.capture());

        Order savedOrder = orderCaptor.getValue();
        assertEquals(OrderStatus.PENDING, savedOrder.getStatus());
        assertNotNull(savedOrder.getOrderedDate());
        assertTrue(savedOrder.getOrderedDate().isBefore(LocalDateTime.now().plusSeconds(1)));
    }

    @Test
    void createOrderLinksEachOrderItemBackToParentOrder() {
        // Arrange a multi-item request so every mapped OrderItem should point back to the same parent order.
        stubSaveReturnsPersistedOrder();
        OrderRequest request = new OrderRequest(
                null,
                21L,
                null,
                List.of(
                        new OrderItemRequest(1L, "Mouse", new BigDecimal("20.00"), 1),
                        new OrderItemRequest(2L, "Keyboard", new BigDecimal("45.00"), 1)
                )
        );

        // Act by creating the order.
        orderService.createOrder(request);

        // Every saved item should keep the back-reference required by the JPA relationship.
        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).save(orderCaptor.capture());

        Order savedOrder = orderCaptor.getValue();
        assertTrue(savedOrder.getOrderItems().stream().allMatch(item -> item.getOrder() == savedOrder));
    }

    @Test
    void createOrderPublishesOrderCreatedEvent() {
        // Arrange a valid request and capture the event emitted after persistence.
        stubSaveReturnsPersistedOrder();
        OrderRequest request = new OrderRequest(
                null,
                33L,
                null,
                List.of(new OrderItemRequest(5L, "Webcam", new BigDecimal("80.00"), 2))
        );

        // Act by creating the order.
        orderService.createOrder(request);

        // The published event should include the persisted order id, owning user, and final total.
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
        // Arrange an order with no line items to cover the reduction starting from BigDecimal.ZERO.
        stubSaveReturnsPersistedOrder();
        OrderRequest request = new OrderRequest(
                null,
                55L,
                null,
                List.of()
        );

        // Act by creating the empty order.
        Order savedOrder = orderService.createOrder(request);

        // The service should preserve the empty item list and compute a zero total.
        assertNotNull(savedOrder);
        assertTrue(savedOrder.getOrderItems().isEmpty());
        assertEquals(BigDecimal.ZERO, savedOrder.getTotalPrice());
        verify(orderRepository).save(any(Order.class));
        verify(eventPublisher).publishEvent(any(OrderCreatedEvent.class));
    }

    @Test
    void createOrderThrowsWhenItemsAreNull() {
        // Arrange a request with null items to cover the current null-handling behavior.
        OrderRequest request = new OrderRequest(
                null,
                66L,
                null,
                null
        );

        // The current implementation streams directly over items, so null should fail fast.
        assertThrows(NullPointerException.class, () -> orderService.createOrder(request));
    }

    @Test
    void getOrdersByUserIdReturnsOnlyOrdersForRequestedUser() {
        // Arrange repository results for one user so the service can delegate the filtering query.
        Order firstOrder = new Order();
        firstOrder.setId(1L);
        firstOrder.setUserId(42L);

        Order secondOrder = new Order();
        secondOrder.setId(2L);
        secondOrder.setUserId(42L);

        Order thirdOrder = new Order();
        thirdOrder.setId(3L);
        thirdOrder.setUserId(123L);

        when(orderRepository.findByUserId(42L))
                .thenReturn(List.of(firstOrder, secondOrder));

        // Act by retrieving the selected user's orders.
        List<Order> userOrders = orderService.getOrdersByUserId(42L);

        // Only the orders for user 42 should be returned.
        assertEquals(2, userOrders.size());
        assertTrue(userOrders.stream()
                .allMatch(order -> order.getUserId().equals(42L)));

        // Verify the service delegated to the correct repository method.
        verify(orderRepository).findByUserId(42L);
    }

    @Test
    void getOrderByIdReturnsOrderWhenFound() {
        // Arrange a stored order so the service can return it unchanged.
        Order order = new Order();
        order.setId(7L);
        order.setUserId(42L);
        when(orderRepository.findById(7L)).thenReturn(Optional.of(order));

        // Act by retrieving the order by id.
        Order result = orderService.getOrderById(7L);

        // The found order should be returned directly.
        assertEquals(7L, result.getId());
        assertEquals(42L, result.getUserId());
        verify(orderRepository).findById(7L);
    }

    @Test
    void getOrderByIdThrowsWhenOrderDoesNotExist() {
        // Arrange a missing id so the service follows its error path.
        when(orderRepository.findById(404L)).thenReturn(Optional.empty());

        // Missing orders should throw the current runtime exception message.
        RuntimeException exception = assertThrows(RuntimeException.class, () -> orderService.getOrderById(404L));
        assertEquals("Order not found", exception.getMessage());
        verify(orderRepository).findById(404L);
    }

    @Test
    void updateOrderStatusUpdatesAndSavesOrder() {
        // Arrange an existing order so the service can update its status and persist the change.
        Order existingOrder = new Order();
        existingOrder.setId(10L);
        existingOrder.setStatus(OrderStatus.PENDING);
        when(orderRepository.findById(10L)).thenReturn(Optional.of(existingOrder));
        when(orderRepository.save(existingOrder)).thenReturn(existingOrder);

        // Act by changing the order status.
        Order updatedOrder = orderService.updateOrderStatus(10L, OrderStatus.PAID);

        // The order should be updated in memory and then saved through the repository.
        assertEquals(OrderStatus.PAID, updatedOrder.getStatus());
        verify(orderRepository).findById(10L);
        verify(orderRepository).save(existingOrder);
    }

    @Test
    void updateOrderStatusThrowsWhenOrderDoesNotExist() {
        // Arrange a missing order id to cover the not-found branch.
        when(orderRepository.findById(999L)).thenReturn(Optional.empty());

        // Updating a missing order should reuse the same not-found exception message.
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> orderService.updateOrderStatus(999L, OrderStatus.PAID));
        assertEquals("Order not found", exception.getMessage());
        verify(orderRepository).findById(999L);
    }

    @Test
    void cancelOrderSetsCancelledStatusAndSavesOrder() {
        // Arrange an existing order so cancelOrder can update it to the cancelled state.
        Order existingOrder = new Order();
        existingOrder.setId(20L);
        existingOrder.setStatus(OrderStatus.PENDING);
        when(orderRepository.findById(20L)).thenReturn(Optional.of(existingOrder));
        when(orderRepository.save(existingOrder)).thenReturn(existingOrder);

        // Act by cancelling the order.
        Order cancelledOrder = orderService.cancelOrder(20L);

        // The service should mark the order as cancelled and persist that change.
        assertEquals(OrderStatus.CANCELLED, cancelledOrder.getStatus());
        verify(orderRepository).findById(20L);
        verify(orderRepository).save(existingOrder);
    }

    @Test
    void cancelOrderThrowsWhenOrderDoesNotExist() {
        // Arrange a missing order id to cover the not-found branch.
        when(orderRepository.findById(888L)).thenReturn(Optional.empty());

        // Cancelling a missing order should throw the current not-found exception.
        RuntimeException exception = assertThrows(RuntimeException.class, () -> orderService.cancelOrder(888L));
        assertEquals("Order not found", exception.getMessage());
        verify(orderRepository).findById(888L);
    }
}
