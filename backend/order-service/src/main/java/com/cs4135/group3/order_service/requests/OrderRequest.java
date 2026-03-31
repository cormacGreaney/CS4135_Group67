package com.cs4135.group3.order_service.requests;

import java.math.BigDecimal;

public record OrderRequest(Long id, Long userId, String orderNumber, String productName, BigDecimal price, Integer quantity)
{

}
