package com.cs4135.group3.user_service.web;

import com.cs4135.group3.user_service.service.UserProfileService;
import com.cs4135.group3.user_service.web.dto.UserResponse;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
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

}
