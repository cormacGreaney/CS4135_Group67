package com.cs4135.group3.order_service.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Order 
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String orderId;

    private Long userId;
    private String orderDate;
    private String orderStatus;
    private Double totalPrice;
}

