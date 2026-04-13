package com.cs4135.group3.user_service.config;

import com.cs4135.group3.user_service.service.BootstrapAdminService;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(0)
public class BootstrapAdminRunner implements ApplicationRunner {

	private final BootstrapAdminService bootstrapAdminService;

	public BootstrapAdminRunner(BootstrapAdminService bootstrapAdminService) {
		this.bootstrapAdminService = bootstrapAdminService;
	}

	@Override
	public void run(ApplicationArguments args) {
		bootstrapAdminService.ensureBootstrapAdmin();
	}
}
