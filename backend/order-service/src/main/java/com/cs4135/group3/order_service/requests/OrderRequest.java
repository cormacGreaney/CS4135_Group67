package com.cs4135.group3.order_service.requests;

import java.math.BigDecimal;
import java.util.List;

public record OrderRequest(
        Long id,
        Long userId,
        String orderNumber,
        List<OrderItemRequest> items)
{

}
