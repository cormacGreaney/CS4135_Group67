package com.cs4135.group3.order_service.requests;

import java.math.BigDecimal;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

public record OrderRequest(
        Long id,
        Long userId,
        String orderNumber,
        @NotBlank String fullName,
        @NotBlank String streetAddress,
        String streetAddress2,
        @NotBlank String cityTown,
        @NotBlank String county,
        @NotBlank String eircode,
        @NotEmpty List<@Valid OrderItemRequest> items)
{

}
