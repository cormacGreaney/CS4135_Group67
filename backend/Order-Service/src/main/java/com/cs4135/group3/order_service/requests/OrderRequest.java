package com.cs4135.group3.order_service.requests;

import java.math.BigDecimal;

public record OrderRequest (Long orderId, Long userId, String orderDate, String orderStatus, BigDecimal totalPrice)
{

}
