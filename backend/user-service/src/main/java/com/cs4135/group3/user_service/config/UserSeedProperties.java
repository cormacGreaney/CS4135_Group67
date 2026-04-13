package com.cs4135.group3.user_service.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.users.seed")
public class UserSeedProperties {

	private boolean enabled = true;

	private String demoPassword = "password12";

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public String getDemoPassword() {
		return demoPassword;
	}

	public void setDemoPassword(String demoPassword) {
		this.demoPassword = demoPassword;
	}
}
