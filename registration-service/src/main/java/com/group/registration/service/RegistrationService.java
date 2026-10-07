package com.group.registration.service;

import com.group.registration.client.StudentClient;
import com.group.registration.client.TopicClient;
import com.group.registration.dto.StudentDTO;
import com.group.registration.dto.TopicDTO;
import com.group.registration.entity.Registration;
import com.group.registration.repository.RegistrationRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class RegistrationService {
    private final RegistrationRepository repository;
    private final StudentClient studentClient;
    private final TopicClient topicClient;

    public RegistrationService(RegistrationRepository repository,
                               StudentClient studentClient,
                               TopicClient topicClient) {
        this.repository = repository;
        this.studentClient = studentClient;
        this.topicClient = topicClient;
    }

    // 1. Lấy tất cả đăng ký
    public List<Registration> getAll() {
        return repository.findAll();
    }

    // 2. Lấy chi tiết 1 đăng ký theo ID
    public Registration getById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đăng ký với id = " + id));
    }

    // 3. Tạo mới đăng ký (có kiểm tra qua Feign Client)
    public Registration create(Registration registration) {
        // 3.1. Gọi Student Service để kiểm tra sinh viên
        StudentDTO student = studentClient.getStudentById(registration.getStudentId());
        if (student == null || !"STUDENT".equals(student.getRole())) {
            throw new RuntimeException("Sinh viên không hợp lệ hoặc không phải là sinh viên!");
        }

        // 3.2. Gọi Topic Service để kiểm tra đề tài
        TopicDTO topic = topicClient.getTopicById(registration.getTopicId());
        if (topic == null) {
            throw new RuntimeException("Đề tài không tồn tại!");
        }
        if (topic.getCurrentStudents() >= topic.getMaxStudents()) {
            throw new RuntimeException("Đề tài đã đủ số lượng sinh viên!");
        }
        if (!"OPEN".equals(topic.getStatus())) {
            throw new RuntimeException("Đề tài đã đóng đăng ký!");
        }

        // 3.3. Kiểm tra trùng lặp trong DB của chính Registration Service
        if (repository.existsByStudentIdAndTopicId(registration.getStudentId(), registration.getTopicId())) {
            throw new RuntimeException("Sinh viên đã đăng ký đề tài này rồi!");
        }

        // 3.4. Lưu đăng ký
        registration.setRegistrationDate(LocalDateTime.now());
        registration.setStatus("PENDING");
        registration.setCreatedAt(LocalDateTime.now());
        registration.setUpdatedAt(LocalDateTime.now());
        return repository.save(registration);
    }

    // 4. Cập nhật trạng thái đăng ký (APPROVED / REJECTED)
    public Registration updateStatus(Long id, String status) {
        Registration registration = getById(id);
        registration.setStatus(status);
        registration.setUpdatedAt(LocalDateTime.now());
        return repository.save(registration);
    }

    // 5. Xóa đăng ký
    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new RuntimeException("Không tìm thấy đăng ký với id = " + id);
        }
        repository.deleteById(id);
    }
}