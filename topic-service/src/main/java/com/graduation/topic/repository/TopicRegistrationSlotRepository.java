package com.graduation.topic.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.graduation.topic.entity.TopicRegistrationSlot;

public interface TopicRegistrationSlotRepository
        extends JpaRepository<TopicRegistrationSlot, Long> {

    boolean existsByTopicIdAndApprovedTrue(Long topicId);

    @Modifying
    @Query("delete from TopicRegistrationSlot s where s.topicId = :topicId")
    void deleteAllForTopic(@Param("topicId") Long topicId);
}
