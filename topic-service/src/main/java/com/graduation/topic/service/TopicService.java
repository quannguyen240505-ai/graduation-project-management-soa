package com.graduation.topic.service;

import java.sql.SQLException;
import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.graduation.topic.client.AuthClient;
import com.graduation.topic.dto.TopicRequest;
import com.graduation.topic.dto.TopicResponse;
import com.graduation.topic.entity.Topic;
import com.graduation.topic.repository.TopicRepository;

@Service
public class TopicService {

    private final TopicRepository repository;
    private final AuthClient authClient;

    public TopicService(
            TopicRepository repository,
            AuthClient authClient) {
        this.repository = repository;
        this.authClient = authClient;
    }

    @Transactional(readOnly = true)
    public List<TopicResponse> getAll() {
        return repository.findAll()
                .stream()
                .map(TopicResponse::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public TopicResponse getById(Long id) {
        Topic topic = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Không tìm thấy đề tài có ID: " + id));

        return TopicResponse.fromEntity(topic);
    }

    public TopicResponse create(TopicRequest request, String token) {
        String topicCode = request.getTopicCode().trim();

        if (repository.existsByTopicCode(topicCode)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Mã đề tài đã tồn tại: " + topicCode);
        }

        AuthClient.UserInfo lecturer = authClient.getUserById(
                request.getLecturerId(),
                token);

        if (!"LECTURER".equals(lecturer.role())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Người được gán phải có vai trò LECTURER");
        }

        if (!"ACTIVE".equals(lecturer.status())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Tài khoản giảng viên đang không hoạt động");
        }

        Topic topic = new Topic();
        topic.setTopicCode(topicCode);
        topic.setTopicName(request.getTopicName().trim());
        topic.setDescription(request.getDescription());
        topic.setLecturerId(request.getLecturerId());
        topic.setMaxStudents(request.getMaxStudents());
        topic.setCurrentStudents(0);
        topic.setField(request.getField());
        topic.setStatus("OPEN");

        try {
            Topic savedTopic = repository.saveAndFlush(topic);
            return TopicResponse.fromEntity(savedTopic);
        } catch (DataIntegrityViolationException ex) {
            if (isDuplicateKey(ex)) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Mã đề tài đã tồn tại: " + topicCode,
                        ex);
            }

            throw ex;
        }
    }

    @Transactional
    public TopicResponse update(
            Long id, TopicRequest request, String token) {

        Topic topic = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Không tìm thấy đề tài có ID: " + id));

        String topicCode = request.getTopicCode().trim();

        if (repository.existsByTopicCodeAndIdNot(topicCode, id)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Mã đề tài đã tồn tại: " + topicCode);
        }

        if (request.getMaxStudents() < topic.getCurrentStudents()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Số lượng tối đa không được nhỏ hơn "
                            + "số sinh viên đã đăng ký");
        }

        AuthClient.UserInfo lecturer = authClient.getUserById(
                request.getLecturerId(),
                token);

        if (!"LECTURER".equals(lecturer.role())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Người được gán phải có vai trò LECTURER");
        }

        if (!"ACTIVE".equals(lecturer.status())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Tài khoản giảng viên đang không hoạt động");
        }

        topic.setTopicCode(topicCode);
        topic.setTopicName(request.getTopicName().trim());
        topic.setDescription(request.getDescription());
        topic.setLecturerId(request.getLecturerId());
        topic.setMaxStudents(request.getMaxStudents());
        topic.setField(request.getField());

        try {
            Topic savedTopic = repository.saveAndFlush(topic);
            return TopicResponse.fromEntity(savedTopic);
        } catch (DataIntegrityViolationException ex) {
            if (isDuplicateKey(ex)) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Mã đề tài đã tồn tại: " + topicCode,
                        ex);
            }

            throw ex;
        }
    }

    @Transactional
    public void delete(Long id) {
        Topic topic = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Không tìm thấy đề tài có ID: " + id));

        if (topic.getCurrentStudents() > 0) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Không thể xóa đề tài đã có sinh viên đăng ký");
        }

        repository.delete(topic);
        repository.flush();
    }

    private boolean isDuplicateKey(Throwable error) {
        Throwable cause = error;

        while (cause != null) {
            if (cause instanceof SQLException sqlException
                    && sqlException.getErrorCode() == 1062) {
                return true;
            }

            cause = cause.getCause();
        }

        return false;
    }
}