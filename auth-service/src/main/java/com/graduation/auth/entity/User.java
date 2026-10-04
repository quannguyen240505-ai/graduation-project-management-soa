package com.graduation.auth.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "users", uniqueConstraints = { @UniqueConstraint(name = "uk_users_user_code", columnNames = "user_code"),
		@UniqueConstraint(name = "uk_users_email", columnNames = "email") })
public class User {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@Column(name = "user_code", nullable = false, length = 30)
	private String userCode;
	@Column(nullable = false, length = 255)
	private String password;
	@Column(name = "full_name", nullable = false, length = 100)
	private String fullName;
	@Column(nullable = false, length = 100)
	private String email;
	@Column(length = 20)
	private String phone;
	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private Role role;
	@Column(name = "class_name", length = 50)
	private String className;
	@Column(length = 100)
	private String department;
	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private UserStatus status;
	@Column(name = "created_at", nullable = false)
	private LocalDateTime createdAt;
	@Column(name = "updated_at", nullable = false)
	private LocalDateTime updatedAt;

	@PrePersist
	void prePersist() {
		var now = LocalDateTime.now();
		createdAt = now;
		updatedAt = now;
		if (status == null)
			status = UserStatus.ACTIVE;
	}

	@PreUpdate
	void preUpdate() {
		updatedAt = LocalDateTime.now();
	}

	public Long getId() {
		return id;
	}

	public String getUserCode() {
		return userCode;
	}

	public void setUserCode(String v) {
		userCode = v;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String v) {
		password = v;
	}

	public String getFullName() {
		return fullName;
	}

	public void setFullName(String v) {
		fullName = v;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String v) {
		email = v;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String v) {
		phone = v;
	}

	public Role getRole() {
		return role;
	}

	public void setRole(Role v) {
		role = v;
	}

	public String getClassName() {
		return className;
	}

	public void setClassName(String v) {
		className = v;
	}

	public String getDepartment() {
		return department;
	}

	public void setDepartment(String v) {
		department = v;
	}

	public UserStatus getStatus() {
		return status;
	}

	public void setStatus(UserStatus v) {
		status = v;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public LocalDateTime getUpdatedAt() {
		return updatedAt;
	}
}
