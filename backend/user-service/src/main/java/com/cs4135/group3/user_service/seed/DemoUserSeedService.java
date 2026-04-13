package com.cs4135.group3.user_service.seed;

import com.cs4135.group3.user_service.config.UserSeedProperties;
import com.cs4135.group3.user_service.domain.User;
import com.cs4135.group3.user_service.domain.UserRole;
import com.cs4135.group3.user_service.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Locale;

@Service
public class DemoUserSeedService {

	private static final Logger log = LoggerFactory.getLogger(DemoUserSeedService.class);

	private static final List<SeedAccount> DEMO_ACCOUNTS = List.of(
			new SeedAccount("customer.demo@example.com", UserRole.CUSTOMER),
			new SeedAccount("customer2.demo@example.com", UserRole.CUSTOMER),
			new SeedAccount("admin.demo@example.com", UserRole.ADMINISTRATOR),
			new SeedAccount("admin2.demo@example.com", UserRole.ADMINISTRATOR));

	private final UserSeedProperties properties;
	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;

	public DemoUserSeedService(
			UserSeedProperties properties,
			UserRepository userRepository,
			PasswordEncoder passwordEncoder) {
		this.properties = properties;
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
	}

	@Transactional
	public void ensureDemoUsers() {
		if (!properties.isEnabled()) {
			return;
		}
		String rawPassword = properties.getDemoPassword();
		if (!StringUtils.hasText(rawPassword)) {
			throw new IllegalStateException(
					"Invalid user seed: set app.users.seed.demo-password (8–128 characters) when seeding is enabled.");
		}
		String password = rawPassword.trim();
		if (password.length() < 8 || password.length() > 128) {
			throw new IllegalStateException(
					"Invalid user seed: demo password must be between 8 and 128 characters.");
		}
		for (SeedAccount account : DEMO_ACCOUNTS) {
			String email = normalizeEmail(account.email());
			if (userRepository.existsByEmailIgnoreCase(email)) {
				log.debug("Demo user seed skipped: {} already exists.", email);
				continue;
			}
			User user = new User();
			user.setEmail(email);
			user.setPasswordHash(passwordEncoder.encode(password));
			user.setRole(account.role());
			userRepository.save(user);
			log.info("Created demo user {} with role {}.", email, account.role());
		}
	}

	private static String normalizeEmail(String email) {
		return email.trim().toLowerCase(Locale.ROOT);
	}

	private record SeedAccount(String email, UserRole role) {
	}
}
