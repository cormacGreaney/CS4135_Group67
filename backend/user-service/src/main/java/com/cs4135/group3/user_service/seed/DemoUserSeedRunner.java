package com.cs4135.group3.user_service.seed;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(1)
@ConditionalOnProperty(prefix = "app.users.seed", name = "enabled", havingValue = "true", matchIfMissing = true)
public class DemoUserSeedRunner implements ApplicationRunner {

	private final DemoUserSeedService demoUserSeedService;

	public DemoUserSeedRunner(DemoUserSeedService demoUserSeedService) {
		this.demoUserSeedService = demoUserSeedService;
	}

	@Override
	public void run(ApplicationArguments args) {
		demoUserSeedService.ensureDemoUsers();
	}
}
