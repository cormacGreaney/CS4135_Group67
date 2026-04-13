package com.cs4135.group3.payment_service.seed;

import javax.sql.DataSource;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.DatabasePopulatorUtils;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.stereotype.Component;

// Runs after Flyway (including V3, which clears the legacy V2 row). Disabled when app.payments.seed.enabled=false.
@Component
@ConditionalOnProperty(prefix = "app.payments.seed", name = "enabled", havingValue = "true", matchIfMissing = true)
public class DemoPaymentSeedRunner implements ApplicationRunner {

	private final DataSource dataSource;

	public DemoPaymentSeedRunner(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	@Override
	public void run(ApplicationArguments args) {
		var populator = new ResourceDatabasePopulator();
		populator.addScript(new ClassPathResource("db/seed/demo_payments.sql"));
		populator.setContinueOnError(false);
		DatabasePopulatorUtils.execute(populator, dataSource);
	}
}
