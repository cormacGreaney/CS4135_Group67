package com.cs4135.group3.order_service;

import static org.assertj.core.api.Assertions.assertThat;

import com.cs4135.group3.order_service.model.Order;
import com.cs4135.group3.order_service.model.OrderStatus;
import com.cs4135.group3.order_service.repository.OrderRepository;
import com.cs4135.group3.order_service.support.AbstractOrderServiceIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@TestPropertySource(
		properties = {
			"app.orders.seed.enabled=true",
			"app.orders.seed.demo-user-id=1"
		})
class DemoOrderSeedIntegrationTest extends AbstractOrderServiceIntegrationTest {

	private static final String DEMO_ORDER_PENDING = "00000000-0000-0000-0000-00000000d001";
	private static final String DEMO_ORDER_PAID = "00000000-0000-0000-0000-00000000d002";

	@Container
	@ServiceConnection
	static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.4");

	@Container
	@ServiceConnection
	static RabbitMQContainer rabbit = new RabbitMQContainer("rabbitmq:3.13-alpine");

	@Autowired
	OrderRepository orderRepository;

	@Test
	void seedsIdempotentDemoOrdersForConfiguredUser() {
		assertThat(orderRepository.existsByOrderNumber(DEMO_ORDER_PENDING)).isTrue();
		assertThat(orderRepository.existsByOrderNumber(DEMO_ORDER_PAID)).isTrue();

		Order pending = orderRepository.findAll().stream()
				.filter(o -> DEMO_ORDER_PENDING.equals(o.getOrderNumber()))
				.findFirst()
				.orElseThrow();
		assertThat(pending.getUserId()).isEqualTo(1L);
		assertThat(pending.getStatus()).isEqualTo(OrderStatus.PENDING);
		assertThat(pending.getOrderItems()).hasSize(1);

		Order paid = orderRepository.findAll().stream()
				.filter(o -> DEMO_ORDER_PAID.equals(o.getOrderNumber()))
				.findFirst()
				.orElseThrow();
		assertThat(paid.getUserId()).isEqualTo(1L);
		assertThat(paid.getStatus()).isEqualTo(OrderStatus.PAID);
		assertThat(paid.getOrderItems()).hasSize(2);
	}
}
