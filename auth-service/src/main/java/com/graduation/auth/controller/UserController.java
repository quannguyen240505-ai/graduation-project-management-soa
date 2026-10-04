package com.graduation.auth.controller;

import com.graduation.auth.dto.UserMeResponse;
import com.graduation.auth.service.UserService;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {

	private final UserService userService;

	public UserController(UserService userService) {
		this.userService = userService;
	}

	@GetMapping("/me")
	public UserMeResponse getMe(@AuthenticationPrincipal Jwt jwt) {

		String userCode = jwt.getSubject();

		return userService.getCurrentUser(userCode);
	}
}