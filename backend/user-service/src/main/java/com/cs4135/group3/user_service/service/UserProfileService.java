package com.cs4135.group3.user_service.service;

import com.cs4135.group3.user_service.repository.UserRepository;
import com.cs4135.group3.user_service.web.dto.UserResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class UserProfileService {

	private final UserRepository userRepository;

	public UserProfileService(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	@Transactional(readOnly = true)
	public UserResponse getById(Long userId) {
		return userRepository.findById(userId)
				.map(u -> new UserResponse(u.getId(), u.getEmail(), u.getRole(), u.getCreatedAt()))
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
	}

}
