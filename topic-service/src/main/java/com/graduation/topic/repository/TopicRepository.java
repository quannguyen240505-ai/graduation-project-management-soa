package com.graduation.topic.repository;

import java.util.Optional;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.graduation.topic.entity.Topic;

/**
 * Truy cập dữ liệu đề tài trong bảng detai.
 */
public interface TopicRepository extends JpaRepository<Topic, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select t from Topic t where t.id = :id")
    Optional<Topic> findByIdForUpdate(@Param("id") Long id);

    /**
     * Kiểm tra mã đề tài đã tồn tại khi tạo mới.
     */
    boolean existsByTopicCode(String topicCode);

    /**
     * Kiểm tra mã trùng với đề tài khác khi cập nhật.
     */
    boolean existsByTopicCodeAndIdNot(String topicCode, Long id);
}
