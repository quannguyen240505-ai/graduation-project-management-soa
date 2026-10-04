package com.graduation.auth.service;

import com.graduation.auth.dto.UserMeResponse;
import com.graduation.auth.entity.User;
import com.graduation.auth.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class UserService {

	private final UserRepository userRepository;

	public UserService(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	public UserMeResponse getCurrentUser(String userCode) {

		User user = userRepository.findByUserCode(userCode).orElseThrow(() -> new RuntimeException("User not found"));

		return new UserMeResponse(user.getId(), user.getUserCode(), user.getFullName(), user.getEmail(),
				user.getPhone(), user.getRole(), user.getClassName(), user.getDepartment(), user.getStatus(),
				user.getCreatedAt(), user.getUpdatedAt());
	}
}