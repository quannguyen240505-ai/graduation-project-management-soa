package com.group.registration.repository;

import com.group.registration.entity.Registration;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RegistrationRepository extends JpaRepository<Registration, Long> {
    // Kiểm tra xem sinh viên đã đăng ký đề tài này chưa
    boolean existsByStudentIdAndTopicId(Long studentId, Long topicId);
}