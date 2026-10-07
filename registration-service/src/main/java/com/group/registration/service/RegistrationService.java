package com.group.registration.service;

import java.util.List;
import java.util.Set;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.group.registration.client.StudentClient;
import com.group.registration.client.TopicClient;
import com.group.registration.dto.StudentDTO;
import com.group.registration.dto.TopicDTO;
import com.group.registration.entity.Registration;
import com.group.registration.repository.RegistrationRepository;

@Service
public class RegistrationService {

    private static final Set<String> ALLOWED_STATUSES =
            Set.of("PENDING", "APPROVED", "REJECTED");

    private final RegistrationRepository repository;
    private final StudentClient studentClient;
    private final TopicClient topicClient;

    public RegistrationService(
            RegistrationRepository repository,
            StudentClient studentClient,
            TopicClient topicClient) {

        this.repository = repository;
        this.studentClient = studentClient;
        this.topicClient = topicClient;
    }

    @Transactional(readOnly = true)
    public List<Registration> getAll() {
        return repository.findAll();
    }

    @Transactional(readOnly = true)
    public Registration getById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Không tìm thấy đăng ký với id = " + id));
    }

    @Transactional
    public Registration create(
            Registration registration,
            String authorization) {

        if (registration.getStudentId() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "studentId không được để trống");
        }

        if (registration.getTopicId() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "topicId không được để trống");
        }

        // Gọi AUTH-SERVICE qua Eureka
        StudentDTO student = studentClient.getStudentById(
                registration.getStudentId(),
                authorization);

        if (student == null
                || !"STUDENT".equals(student.getRole())
                || !"ACTIVE".equals(student.getStatus())) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Sinh viên không tồn tại, không phải STUDENT "
                    + "hoặc tài khoản không ACTIVE");
        }

        // Gọi TOPIC-SERVICE qua Eureka
        TopicDTO topic = topicClient.getTopicById(
                registration.getTopicId(),
                authorization);

        if (topic == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Đề tài không tồn tại");
        }

        if (!"OPEN".equals(topic.getStatus())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Đề tài đã đóng đăng ký");
        }

        if (topic.getCurrentStudents() == null
                || topic.getMaxStudents() == null) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "Topic Service trả về dữ liệu không hợp lệ");
        }

        if (topic.getCurrentStudents()
                >= topic.getMaxStudents()) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Đề tài đã đủ số lượng sinh viên");
        }

        if (repository.existsByStudentIdAndTopicId(
                registration.getStudentId(),
                registration.getTopicId())) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Sinh viên đã đăng ký đề tài này rồi");
        }

        registration.setId(null);
        registration.setStatus("PENDING");

        return repository.save(registration);
    }

    @Transactional
    public Registration updateStatus(
            Long id,
            String status) {

        if (status == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "status không được để trống");
        }

        String normalizedStatus =
                status.trim().toUpperCase();

        if (!ALLOWED_STATUSES.contains(normalizedStatus)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Status chỉ được phép là "
                    + "PENDING, APPROVED hoặc REJECTED");
        }

        Registration registration = getById(id);
        registration.setStatus(normalizedStatus);

        return repository.save(registration);
    }

    @Transactional
    public void delete(Long id) {
        Registration registration = getById(id);
        repository.delete(registration);
    }
}