package com.graduation.auth.dto;

import com.graduation.auth.entity.User;

/** Thông tin người dùng phục vụ việc kiểm tra giữa các service. */
public class UserLookupResponse {

    private final Long id;
    private final String role;
    private final String status;

    public UserLookupResponse(User user) {
        this.id = user.getId();
        this.role = user.getRole().name();
        this.status = user.getStatus().name();
    }

    public Long getId() {
        return id;
    }

    public String getRole() {
        return role;
    }

    public String getStatus() {
        return status;
    }
}