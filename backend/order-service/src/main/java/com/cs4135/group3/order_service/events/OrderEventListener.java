package com.cs4135.group3.order_service.events;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class OrderEventListener
{

    @EventListener
    public void handleOrderCreated(OrderCreatedEvent event)
    {
        log.info("Order Created Event Received: {}", event);
    }
}