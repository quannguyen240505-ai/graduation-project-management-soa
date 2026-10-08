package com.group.registration.service;

import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.security.oauth2.jwt.Jwt;

import com.group.registration.client.StudentClient;
import com.group.registration.client.TopicClient;
import com.group.registration.dto.StudentDTO;
import com.group.registration.dto.TopicDTO;
import com.group.registration.dto.TopicCapacityRequest;
import com.group.registration.entity.Registration;
import com.group.registration.repository.RegistrationRepository;
import feign.FeignException;

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

    // Sinh viên chỉ được xem đăng ký của chính mình.
    // Giảng viên và quản trị viên có thể xem toàn bộ để quản lý/duyệt.
    @Transactional(readOnly = true)
    public List<Registration> getAll(Jwt jwt) {
        Long userId = authenticatedUserId(jwt);
        String role = jwt.getClaimAsString("role");
        if ("STUDENT".equals(role)) {
            return repository.findByStudentId(userId);
        }
        if ("LECTURER".equals(role) || "ADMIN".equals(role)) {
            return repository.findAll();
        }
        throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                "Không có quyền xem danh sách đăng ký");
    }

    @Transactional(readOnly = true)
    public Registration getById(Long id, Jwt jwt) {
        Long userId = authenticatedUserId(jwt);
        String role = jwt.getClaimAsString("role");
        if (!"STUDENT".equals(role)
                && !"LECTURER".equals(role)
                && !"ADMIN".equals(role)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Không có quyền xem đăng ký");
        }
        Registration registration = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Không tìm thấy đăng ký với id = " + id));
        if ("STUDENT".equals(role)
                && !userId.equals(registration.getStudentId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Không thể xem đăng ký của sinh viên khác");
        }
        return registration;
    }

    @Transactional
    public Registration create(
            Registration registration,
            Jwt jwt,
            String authorization) {

        // Chỉ dùng danh tính lấy từ JWT đã được xác thực, không tin studentId từ body.
        Long authenticatedUserId = authenticatedUserId(jwt);
        if (!"STUDENT".equals(jwt.getClaimAsString("role"))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Chỉ sinh viên được đăng ký đề tài");
        }

        // Vẫn có thể nhận studentId để tương thích Postman/FE cũ,
        // nhưng không bao giờ cho phép ghi đè danh tính từ token.
        if (registration.getStudentId() != null
                && !authenticatedUserId.equals(registration.getStudentId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Không thể đăng ký đề tài cho sinh viên khác");
        }

        if (registration.getTopicId() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "topicId không được để trống");
        }

        // Gọi AUTH-SERVICE qua Eureka
        StudentDTO currentUser =
                studentClient.getCurrentStudent(authorization);

        if (currentUser == null
                || currentUser.getId() == null
                || !authenticatedUserId.equals(currentUser.getId())
                || !"STUDENT".equals(currentUser.getRole())
                || !"ACTIVE".equals(currentUser.getStatus())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Tài khoản không có quyền đăng ký đề tài");
        }

        registration.setStudentId(authenticatedUserId);

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
        registration.setCapacityVersion(0);

        return repository.save(registration);
    }

    /** Lấy định danh an toàn từ JWT đã được Spring Security xác thực. */
    private Long authenticatedUserId(Jwt jwt) {
        if (jwt == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                    "Thiếu JWT hợp lệ");
        }
        // Claim số có thể được giải mã thành Integer hoặc Long.
        Object claim = jwt.getClaim("userId");
        if (!(claim instanceof Number number)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                    "Token thiếu userId hợp lệ");
        }
        return number.longValue();
    }

    @Transactional
    public Registration updateStatus(
            Long id,
            String status,
            String authorization) {

        if (status == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "status không được để trống");
        }

        String normalizedStatus =
                status.trim().toUpperCase(Locale.ROOT);

        if (!ALLOWED_STATUSES.contains(normalizedStatus)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Status chỉ được phép là "
                    + "PENDING, APPROVED hoặc REJECTED");
        }

        Registration registration = findForUpdate(id);
        long version = registration.getCapacityVersion();

        // Đăng ký APPROVED cũ (version=0) cũng có thể được đồng bộ bằng
        // cách gửi lại APPROVED sau khi áp dụng bản sửa.
        if (!normalizedStatus.equals(registration.getStatus())
                || ("APPROVED".equals(normalizedStatus) && version == 0)) {
            version = nextVersion(version);
        }

        synchronizeCapacity(registration, normalizedStatus, version, authorization);

        registration.setStatus(normalizedStatus);
        registration.setCapacityVersion(version);

        return repository.saveAndFlush(registration);
    }

    @Transactional
    public void delete(Long id, String authorization) {
        Registration registration = findForUpdate(id);
        try {
            synchronizeCapacity(registration, "REJECTED",
                    nextVersion(registration.getCapacityVersion()), authorization);
        } catch (ResponseStatusException ex) {
            // Một đề tài chưa có chỗ được tính có thể đã bị xóa bằng CRUD cũ.
            // Không bỏ qua lỗi khác hoặc lỗi 404 của đăng ký đang APPROVED.
            if (ex.getStatusCode().value() != 404
                    || "APPROVED".equals(registration.getStatus())) {
                throw ex;
            }
        }
        repository.delete(registration);
        repository.flush();
    }

    private Registration findForUpdate(Long id) {
        return repository.findByIdForUpdate(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Không tìm thấy đăng ký với id = " + id));
    }

    private long nextVersion(long version) {
        if (version == Long.MAX_VALUE) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "capacityVersion đã vượt giới hạn");
        }
        return version + 1;
    }

    private void synchronizeCapacity(
            Registration registration, String status,
            long version, String authorization) {

        if (authorization == null || !authorization.startsWith("Bearer ")
                || authorization.substring(7).isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Thiếu Bearer JWT");
        }

        try {
            TopicDTO topic = topicClient.synchronizeCapacity(
                    registration.getTopicId(), registration.getId(),
                    new TopicCapacityRequest("APPROVED".equals(status), version),
                    authorization);
            if (topic == null) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_GATEWAY, "Topic Service trả về response rỗng");
            }
        } catch (FeignException ex) {
            int code = ex.status();
            if (code == 400 || code == 401 || code == 403 || code == 404 || code == 409) {
                throw new ResponseStatusException(
                        HttpStatus.valueOf(code),
                        "Topic Service từ chối cập nhật số chỗ (HTTP " + code + ")", ex);
            }
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "Chưa đồng bộ được số chỗ. Hãy gửi lại cùng yêu cầu khi Topic hoạt động.",
                    ex);
        }
    }
}
