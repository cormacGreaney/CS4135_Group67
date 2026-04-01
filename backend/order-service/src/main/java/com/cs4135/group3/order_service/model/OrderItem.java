package com.cs4135.group3.order_service.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "order_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderItem
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long productId;

    private String productName;

    private BigDecimal price;

    private Integer quantity;

    // Each order item belongs to exactly one order via the order_id foreign key.
    @ManyToOne
    @JoinColumn(name = "order_id")
    private Order order;
}
