package com.cs4135.group3.user_service.config;

import com.cs4135.group3.user_service.service.BootstrapAdminService;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
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
