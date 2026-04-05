package com.cs4135.group3.product_service.seed;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.DatabasePopulatorUtils;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

// Runs after the context (and Flyway) are up. Disabled when app.catalog.seed-alcohol.enabled=false.
@Component
@ConditionalOnProperty(prefix = "app.catalog.seed-alcohol", name = "enabled", havingValue = "true", matchIfMissing = true)
public class AlcoholCatalogSeedRunner implements ApplicationRunner {

	private final DataSource dataSource;

	public AlcoholCatalogSeedRunner(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	@Override
	public void run(ApplicationArguments args) {
		var populator = new ResourceDatabasePopulator();
		populator.addScript(new ClassPathResource("db/seed/alcohol_products.sql"));
		populator.setContinueOnError(false);
		DatabasePopulatorUtils.execute(populator, dataSource);
	}
}
