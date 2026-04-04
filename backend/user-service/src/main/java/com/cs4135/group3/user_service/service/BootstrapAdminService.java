package com.cs4135.group3.user_service.service;

import com.cs4135.group3.user_service.config.BootstrapAdminProperties;
import com.cs4135.group3.user_service.domain.User;
import com.cs4135.group3.user_service.domain.UserRole;
import com.cs4135.group3.user_service.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Locale;

@Service
public class BootstrapAdminService {

	private static final Logger log = LoggerFactory.getLogger(BootstrapAdminService.class);

	private final BootstrapAdminProperties properties;
	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;

	public BootstrapAdminService(
			BootstrapAdminProperties properties,
			UserRepository userRepository,
			PasswordEncoder passwordEncoder) {
		this.properties = properties;
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
	}

	@Transactional
	public void ensureBootstrapAdmin() {
		String rawEmail = properties.getEmail();
		String rawPassword = properties.getPassword();
		boolean hasEmail = StringUtils.hasText(rawEmail);
		boolean hasPassword = StringUtils.hasText(rawPassword);
		if (!hasEmail && !hasPassword) {
			log.debug("Admin bootstrap disabled (no email/password configured).");
			return;
		}
		if (hasEmail != hasPassword) {
			throw new IllegalStateException(
					"Invalid admin bootstrap: set both app.bootstrap.admin.email and app.bootstrap.admin.password, or neither.");
		}
		String email = normalizeEmail(rawEmail);
		String password = rawPassword.trim();
		if (password.length() < 8 || password.length() > 128) {
			throw new IllegalStateException(
					"Invalid admin bootstrap: password must be between 8 and 128 characters.");
		}
		if (userRepository.existsByEmailIgnoreCase(email)) {
			userRepository.findByEmailIgnoreCase(email).ifPresent(user -> {
				if (user.getRole() == UserRole.ADMINISTRATOR) {
					log.info("Admin bootstrap skipped: user {} already exists as ADMINISTRATOR.", email);
				} else {
					log.warn(
							"Admin bootstrap skipped: email {} is already registered with role {}. "
									+ "Use a different BOOTSTRAP_ADMIN_EMAIL or update the account in the database.",
							email,
							user.getRole());
				}
			});
			return;
		}
		User admin = new User();
		admin.setEmail(email);
		admin.setPasswordHash(passwordEncoder.encode(password));
		admin.setRole(UserRole.ADMINISTRATOR);
		userRepository.save(admin);
		log.info("Created bootstrap administrator account for {}.", email);
	}

	private static String normalizeEmail(String email) {
		return email.trim().toLowerCase(Locale.ROOT);
	}
}
