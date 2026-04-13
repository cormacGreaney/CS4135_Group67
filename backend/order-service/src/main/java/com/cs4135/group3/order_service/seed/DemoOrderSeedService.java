package com.cs4135.group3.order_service.seed;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cs4135.group3.order_service.config.OrderSeedProperties;
import com.cs4135.group3.order_service.model.Order;
import com.cs4135.group3.order_service.model.OrderItem;
import com.cs4135.group3.order_service.model.OrderStatus;
import com.cs4135.group3.order_service.repository.OrderRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class DemoOrderSeedService {

	private static final String DEMO_ORDER_NUMBER_PENDING = "00000000-0000-0000-0000-00000000d001";
	private static final String DEMO_ORDER_NUMBER_PAID = "00000000-0000-0000-0000-00000000d002";

	private static final UUID PRODUCT_DEMO_A = UUID.fromString("00000000-0000-0000-0000-000000000001");
	private static final UUID PRODUCT_DEMO_B = UUID.fromString("00000000-0000-0000-0000-000000000002");

	private final OrderSeedProperties properties;
	private final OrderRepository orderRepository;

	@Transactional
	public void ensureDemoOrders() {
		if (!properties.isEnabled()) {
			return;
		}
		long userId = properties.getDemoUserId();
		ensurePendingDemoOrder(userId);
		ensurePaidDemoOrder(userId);
	}

	private void ensurePendingDemoOrder(long userId) {
		if (orderRepository.existsByOrderNumber(DEMO_ORDER_NUMBER_PENDING)) {
			log.debug("Demo order seed skipped: order_number {} already exists.", DEMO_ORDER_NUMBER_PENDING);
			return;
		}
		Order order = baseOrder(userId, DEMO_ORDER_NUMBER_PENDING, OrderStatus.PENDING, LocalDateTime.now().minusDays(1));
		OrderItem line = new OrderItem();
		line.setProductId(PRODUCT_DEMO_A);
		line.setProductName("Demo Irish Whiskey (seed)");
		line.setPrice(new BigDecimal("42.00"));
		line.setQuantity(2);
		line.setOrder(order);
		order.setOrderItems(new ArrayList<>(List.of(line)));
		order.setTotalPrice(new BigDecimal("84.00"));
		orderRepository.save(order);
		log.info("Created demo PENDING order {} for userId={}.", DEMO_ORDER_NUMBER_PENDING, userId);
	}

	private void ensurePaidDemoOrder(long userId) {
		if (orderRepository.existsByOrderNumber(DEMO_ORDER_NUMBER_PAID)) {
			log.debug("Demo order seed skipped: order_number {} already exists.", DEMO_ORDER_NUMBER_PAID);
			return;
		}
		Order order = baseOrder(userId, DEMO_ORDER_NUMBER_PAID, OrderStatus.PAID, LocalDateTime.now().minusDays(3));
		OrderItem first = new OrderItem();
		first.setProductId(PRODUCT_DEMO_A);
		first.setProductName("Demo Stout 4-pack (seed)");
		first.setPrice(new BigDecimal("12.50"));
		first.setQuantity(1);
		first.setOrder(order);
		OrderItem second = new OrderItem();
		second.setProductId(PRODUCT_DEMO_B);
		second.setProductName("Demo Gin 700ml (seed)");
		second.setPrice(new BigDecimal("35.00"));
		second.setQuantity(1);
		second.setOrder(order);
		order.setOrderItems(new ArrayList<>(List.of(first, second)));
		order.setTotalPrice(new BigDecimal("47.50"));
		orderRepository.save(order);
		log.info("Created demo PAID order {} for userId={}.", DEMO_ORDER_NUMBER_PAID, userId);
	}

	private static Order baseOrder(long userId, String orderNumber, OrderStatus status, LocalDateTime orderedDate) {
		Order order = new Order();
		order.setUserId(userId);
		order.setOrderNumber(orderNumber);
		order.setStatus(status);
		order.setOrderedDate(orderedDate);
		order.setFullName("Demo Customer");
		order.setStreetAddress("12 Student Courtyard");
		order.setStreetAddress2(null);
		order.setCityTown("Newcastle");
		order.setCounty("County Limerick");
		order.setEircode("V94 E2YR");
		return order;
	}
}
