package com.cs4135.group3.order_service.repository;

import com.cs4135.group3.order_service.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long>
{
}
