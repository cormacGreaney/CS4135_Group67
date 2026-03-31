
import com.cs4135.group3.order_service.model.Order;
import com.cs4135.group3.order_service.repository.OrderRepository;
import com.cs4135.group3.order_service.requests.OrderRequest;
import com.cs4135.group3.order_service.service.OrderService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private OrderService orderService;

    @Test
    void createOrderMapsRequestAndSavesOrder() {
        // Arrange a request with known values so we can verify the service copies them into the saved Order.
        OrderRequest request = new OrderRequest(
                1L,
                42L,
                "ignored",
                "SKU-123",
                new BigDecimal("49.99"),
                3
        );

        // Act by creating the order through the service.
        orderService.createOrder(request);

        // Capture the Order sent to the repository and assert the mapped fields on that saved object.
        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).save(orderCaptor.capture());

        Order savedOrder = orderCaptor.getValue();
        assertEquals(42L, savedOrder.getUserId());
        assertEquals("SKU-123", savedOrder.getProductName());
        assertEquals(new BigDecimal("49.99"), savedOrder.getPrice());
        assertEquals(3, savedOrder.getQuantity());
        assertNull(savedOrder.getId());
        assertNotNull(savedOrder.getOrderNumber());
        assertDoesNotThrow(() -> UUID.fromString(savedOrder.getOrderNumber()));
    }

    @Test
    void createOrderGeneratesNewOrderNumberInsteadOfUsingRequestValue() {
        // Arrange a request that already has an order number so we can confirm the service replaces it.
        OrderRequest request = new OrderRequest(
                99L,
                77L,
                "request-order-number",
                "Desk Lamp",
                new BigDecimal("15.50"),
                1
        );

        // Act by creating the order.
        orderService.createOrder(request);

        // Capture the saved Order and verify a fresh UUID was generated instead of using the request value.
        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).save(orderCaptor.capture());

        Order savedOrder = orderCaptor.getValue();
        assertNotEquals("request-order-number", savedOrder.getOrderNumber());
        assertDoesNotThrow(() -> UUID.fromString(savedOrder.getOrderNumber()));
    }

    @Test
    void createOrderSavesExactlyOneOrder() {
        // Arrange a valid request and verify the service only performs one repository save call.
        OrderRequest request = new OrderRequest(
                5L,
                11L,
                "unused",
                "Keyboard",
                new BigDecimal("120.00"),
                2
        );

        // Act by creating the order once.
        orderService.createOrder(request);

        // Assert that exactly one save happened and no extra repository interactions were triggered.
        verify(orderRepository).save(org.mockito.ArgumentMatchers.any(Order.class));
        verifyNoMoreInteractions(orderRepository);
    }

    @Test
    void getOrdersByUserIdReturnsOnlyOrdersForRequestedUser() {
        // Arrange two orders for the same user and one order for a different user.
        Order firstOrder = new Order(1L, 42L, "order-1", "Mouse", new BigDecimal("20.00"), 1);
        Order secondOrder = new Order(2L, 42L, "order-2", "Monitor", new BigDecimal("150.00"), 1);
        Order thirdOrder = new Order(3L, 123L, "order-3", "Case", new BigDecimal("250.00"), 1);
        when(orderRepository.findByUserId(42L)).thenReturn(List.of(firstOrder, secondOrder));

        // Act by retrieving orders for the selected user.
        List<Order> userOrders = orderService.getOrdersByUserId(42L);

        // Assert that the service returns the specific user's orders and delegates to the repository filter.
        assertEquals(2, userOrders.size());
        assertTrue(userOrders.stream().allMatch(order -> order.getUserId().equals(42L)));
        assertEquals("Mouse", userOrders.get(0).getProductName());
        assertEquals("Monitor", userOrders.get(1).getProductName());
        verify(orderRepository).findByUserId(42L);
    }
}
