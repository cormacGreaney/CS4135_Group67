package com.cs4135.group3.product_service;

import com.cs4135.group3.product_service.config.JwtProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

// Product catalog API. Data lives in MySQL; table structure comes from Flyway scripts.
// Logins and tokens come from user-service — we use the same signing key to check those tokens.
@SpringBootApplication
@EnableConfigurationProperties(JwtProperties.class)
public class ProductServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(ProductServiceApplication.class, args);
	}
}
