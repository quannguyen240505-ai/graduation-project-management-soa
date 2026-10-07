package com.group.registration.repository;

import com.group.registration.entity.Registration;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RegistrationRepository
        extends JpaRepository<Registration, Long> {

    boolean existsByStudentIdAndTopicId(
            Long studentId,
            Long topicId);
}