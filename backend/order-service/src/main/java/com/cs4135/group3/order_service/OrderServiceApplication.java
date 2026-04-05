package com.cs4135.group3.order_service;

import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import com.cs4135.group3.order_service.config.JwtProperties;

@SpringBootApplication
@EnableRabbit
@EnableConfigurationProperties(JwtProperties.class)
public class OrderServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(OrderServiceApplication.class, args);
	}

}
