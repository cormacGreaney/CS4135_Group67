package com.cs4135.group3.user_service.service;

import com.cs4135.group3.user_service.domain.User;
import com.cs4135.group3.user_service.repository.UserRepository;
import com.cs4135.group3.user_service.web.dto.ChangePasswordRequest;
import com.cs4135.group3.user_service.web.dto.UserResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class UserProfileService {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;

	public UserProfileService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
	}

	@Transactional(readOnly = true)
	public UserResponse getById(Long userId) {
		return userRepository.findById(userId)
				.map(u -> new UserResponse(u.getId(), u.getEmail(), u.getRole(), u.getCreatedAt()))
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
	}

	@Transactional
	public void changePassword(Long userId, ChangePasswordRequest request) {
		User user = userRepository.findById(userId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
		if (!passwordEncoder.matches(request.currentPassword(), user.getPasswordHash())) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Current password is incorrect");
		}
		if (passwordEncoder.matches(request.newPassword(), user.getPasswordHash())) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "New password must be different from the current password");
		}
		user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
		userRepository.save(user);
	}

}

