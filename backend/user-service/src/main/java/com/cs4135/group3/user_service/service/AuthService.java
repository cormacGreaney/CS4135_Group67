package com.cs4135.group3.user_service.service;

import com.cs4135.group3.user_service.config.JwtProperties;
import com.cs4135.group3.user_service.domain.User;
import com.cs4135.group3.user_service.domain.UserRole;
import com.cs4135.group3.user_service.exception.EmailAlreadyExistsException;
import com.cs4135.group3.user_service.repository.UserRepository;
import com.cs4135.group3.user_service.web.dto.AuthResponse;
import com.cs4135.group3.user_service.web.dto.LoginRequest;
import com.cs4135.group3.user_service.web.dto.RegisterRequest;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
public class AuthService {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final TokenService tokenService;
	private final JwtProperties jwtProperties;

	public AuthService(
			UserRepository userRepository,
			PasswordEncoder passwordEncoder,
			TokenService tokenService,
			JwtProperties jwtProperties) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
		this.tokenService = tokenService;
		this.jwtProperties = jwtProperties;
	}

	@Transactional
	public AuthResponse register(RegisterRequest request) {
		String email = normalizeEmail(request.email());
		if (userRepository.existsByEmailIgnoreCase(email)) {
			throw new EmailAlreadyExistsException();
		}
		User user = new User();
		user.setEmail(email);
		user.setPasswordHash(passwordEncoder.encode(request.password()));
		user.setRole(UserRole.CUSTOMER);
		User saved = userRepository.save(user);
		return buildAuthResponse(saved);
	}

	@Transactional(readOnly = true)
	public AuthResponse login(LoginRequest request) {
		String email = normalizeEmail(request.email());
		User user = userRepository.findByEmailIgnoreCase(email)
				.orElseThrow(() -> new BadCredentialsException("Invalid credentials"));
		if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
			throw new BadCredentialsException("Invalid credentials");
		}
		return buildAuthResponse(user);
	}

	private AuthResponse buildAuthResponse(User user) {
		String token = tokenService.generateAccessToken(user);
		long expiresInSeconds = Math.max(1L, jwtProperties.getExpirationMs() / 1000);
		return new AuthResponse(token, "Bearer", expiresInSeconds);
	}

	private static String normalizeEmail(String email) {
		return email.trim().toLowerCase(Locale.ROOT);
	}

}
