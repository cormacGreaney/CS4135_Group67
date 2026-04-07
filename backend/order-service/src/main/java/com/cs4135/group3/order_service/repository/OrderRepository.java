package com.cs4135.group3.order_service.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.cs4135.group3.order_service.model.Order;
import com.cs4135.group3.order_service.model.OrderStatus;

public interface OrderRepository extends JpaRepository<Order, Long>
{
	@EntityGraph("Order.withItems")
	@Override
	List<Order> findAll();

	@EntityGraph("Order.withItems")
	@Override
	Optional<Order> findById(Long id);

	@EntityGraph("Order.withItems")
	List<Order> findByUserId(Long userId);

	@EntityGraph("Order.withItems")
	List<Order> findByStatus(OrderStatus status);
}
