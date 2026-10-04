package com.graduation.auth.service;

import com.graduation.auth.dto.UserLookupResponse;
import com.graduation.auth.entity.User;
import com.graduation.auth.repository.UserRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

/** Cung cấp thông tin người dùng cho các service khác. */
@Service
public class UserLookupService {

    private final UserRepository repository;

    public UserLookupService(UserRepository repository) {
        this.repository = repository;
    }

    /** Tìm người dùng theo ID; trả lỗi 404 nếu không tồn tại. */
    @Transactional(readOnly = true)
    public UserLookupResponse getById(Long id) {
        User user = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Không tìm thấy người dùng có ID: " + id));

        return new UserLookupResponse(user);
    }
}