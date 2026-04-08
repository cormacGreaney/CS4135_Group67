package com.cs4135.group3.user_service.web;

import com.cs4135.group3.user_service.service.UserProfileService;
import com.cs4135.group3.user_service.web.dto.ChangePasswordRequest;
import com.cs4135.group3.user_service.web.dto.UserResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {

	private final UserProfileService userProfileService;

	public UserController(UserProfileService userProfileService) {
		this.userProfileService = userProfileService;
	}

	@GetMapping("/me")
	public UserResponse getCurrentUser(Authentication authentication) {
		Long userId = Long.parseLong(authentication.getName());
		return userProfileService.getById(userId);
	}

	@PutMapping("/me/password")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void changePassword(Authentication authentication, @Valid @RequestBody ChangePasswordRequest request) {
		Long userId = Long.parseLong(authentication.getName());
		userProfileService.changePassword(userId, request);
	}

}
