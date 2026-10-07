package com.graduation.topic.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.graduation.topic.entity.Topic;

/**
 * Truy cập dữ liệu đề tài trong bảng detai.
 */
public interface TopicRepository extends JpaRepository<Topic, Long> {

    /**
     * Kiểm tra mã đề tài đã tồn tại khi tạo mới.
     */
    boolean existsByTopicCode(String topicCode);

    /**
     * Kiểm tra mã trùng với đề tài khác khi cập nhật.
     */
    boolean existsByTopicCodeAndIdNot(String topicCode, Long id);
}