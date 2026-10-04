package com.graduation.auth.controller;

import com.graduation.auth.dto.UserLookupResponse;
import com.graduation.auth.service.UserLookupService;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Cung cấp API tra cứu người dùng cho các service khác. */
@RestController
@RequestMapping("/api/auth/users")
public class UserLookupController {

    private final UserLookupService service;

    public UserLookupController(UserLookupService service) {
        this.service = service;
    }

    /** Trả thông tin người dùng theo ID. */
    @GetMapping("/{id}")
    public UserLookupResponse getById(@PathVariable("id") Long id) {
        return service.getById(id);
    }
}