package com.group.registration.repository;

import com.group.registration.entity.Registration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;

public interface RegistrationRepository extends JpaRepository<Registration, Long> {

    boolean existsByStudentIdAndTopicId(Long studentId, Long topicId);

    List<Registration> findByStudentId(Long studentId);

    // Method mới: tìm các đăng ký PENDING khác của cùng sinh viên
    List<Registration> findByStudentIdAndStatusAndIdNot(
            Long studentId, String status, Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT r FROM Registration r WHERE r.id = :id")
    Optional<Registration> findByIdForUpdate(@Param("id") Long id);
}