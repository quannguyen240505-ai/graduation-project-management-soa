package com.graduation.topic.service;

import java.util.Objects;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.graduation.topic.dto.TopicCapacityRequest;
import com.graduation.topic.dto.TopicResponse;
import com.graduation.topic.entity.Topic;
import com.graduation.topic.entity.TopicRegistrationSlot;
import com.graduation.topic.repository.TopicRegistrationSlotRepository;
import com.graduation.topic.repository.TopicRepository;

@Service
public class TopicCapacityService {

    private final TopicRepository topics;
    private final TopicRegistrationSlotRepository slots;

    public TopicCapacityService(
            TopicRepository topics, TopicRegistrationSlotRepository slots) {
        this.topics = topics;
        this.slots = slots;
    }

    @Transactional
    public TopicResponse synchronize(
            Long topicId, Long registrationId, TopicCapacityRequest request,
            Long callerId, String role) {

        if (topicId == null || topicId <= 0
                || registrationId == null || registrationId <= 0
                || request == null || request.approved() == null
                || request.version() == null || request.version() < 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "ID, approved hoặc version không hợp lệ");
        }

        // Khóa cùng hàng detai mà CRUD update/delete cũng sử dụng.
        Topic topic = topics.findByIdForUpdate(topicId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Không tìm thấy đề tài: " + topicId));

        if (!"ADMIN".equals(role)
                && !("LECTURER".equals(role)
                    && callerId != null
                    && Objects.equals(callerId, topic.getLecturerId()))) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Chỉ ADMIN hoặc giảng viên phụ trách đề tài được cập nhật số chỗ");
        }

        TopicRegistrationSlot slot = slots.findById(registrationId).orElse(null);
        if (slot != null && !Objects.equals(slot.getTopicId(), topicId)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "ID đăng ký đã gắn với đề tài khác");
        }

        TopicCapacityPolicy.PreviousState previous = slot == null ? null
                : new TopicCapacityPolicy.PreviousState(
                        slot.isApproved(), slot.getCapacityVersion());

        int count;
        try {
            count = TopicCapacityPolicy.calculate(
                    topic.getCurrentStudents(), topic.getMaxStudents(),
                    "OPEN".equals(topic.getStatus()), previous,
                    request.approved(), request.version());
        } catch (IllegalStateException ex) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, ex.getMessage(), ex);
        }

        if (slot == null) {
            slot = new TopicRegistrationSlot(registrationId, topicId);
        }
        slot.setApproved(request.approved());
        slot.setCapacityVersion(request.version());
        topic.setCurrentStudents(count);

        // Lịch sử và counter nằm trong cùng transaction của Topic Service.
        slots.saveAndFlush(slot);
        topics.saveAndFlush(topic);
        return TopicResponse.fromEntity(topic);
    }
}
