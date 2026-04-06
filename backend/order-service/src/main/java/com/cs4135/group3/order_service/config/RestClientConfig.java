package com.cs4135.group3.order_service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestClientConfig {

	@Bean
	RestTemplate restTemplate() {
		return new RestTemplate();
	}
}
