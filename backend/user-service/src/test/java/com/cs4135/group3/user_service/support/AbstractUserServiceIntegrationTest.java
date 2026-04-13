package com.cs4135.group3.user_service.support;

import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(
		properties = {
			"app.users.seed.enabled=false",
			"app.jwt.secret=0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef",
			"app.jwt.expiration-ms=86400000"
		})
@AutoConfigureMockMvc
public abstract class AbstractUserServiceIntegrationTest {
}
