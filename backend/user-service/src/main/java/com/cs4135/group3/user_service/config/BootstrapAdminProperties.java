package com.cs4135.group3.user_service.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** Optional admin user created at startup. Set both fields (via env) or leave unset. */
@ConfigurationProperties(prefix = "app.bootstrap.admin")
public class BootstrapAdminProperties {

	private String email = "";

	private String password = "";

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
}
