package com.cs4135.group3.order_service.support;

import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(
		properties = {
			"app.orders.seed.enabled=false",
			"app.jwt.secret=0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef",
			"app.jwt.expiration-ms=86400000"
		})
public abstract class AbstractOrderServiceIntegrationTest {
}
