package com.cs4135.group3.order_service.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.orders.seed")
public class OrderSeedProperties {

	private boolean enabled = true;

	private long demoUserId = 1L;

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public long getDemoUserId() {
		return demoUserId;
	}

	public void setDemoUserId(long demoUserId) {
		this.demoUserId = demoUserId;
	}
}
