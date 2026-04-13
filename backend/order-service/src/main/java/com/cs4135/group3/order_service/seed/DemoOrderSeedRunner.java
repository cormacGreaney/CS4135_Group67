package com.cs4135.group3.order_service.seed;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(0)
@ConditionalOnProperty(prefix = "app.orders.seed", name = "enabled", havingValue = "true", matchIfMissing = true)
public class DemoOrderSeedRunner implements ApplicationRunner {

	private final DemoOrderSeedService demoOrderSeedService;

	public DemoOrderSeedRunner(DemoOrderSeedService demoOrderSeedService) {
		this.demoOrderSeedService = demoOrderSeedService;
	}

	@Override
	public void run(ApplicationArguments args) {
		demoOrderSeedService.ensureDemoOrders();
	}
}
