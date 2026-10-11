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

    @Transactional(readOnly = true)
    public List<Registration> getAll(Jwt jwt) {
        Long userId = authenticatedUserId(jwt);
        String role = jwt.getClaimAsString("role");

        // DEBUG LOG
        System.out.println("===== GET REGISTRATIONS =====");
        System.out.println("User ID: " + userId);
        System.out.println("Role: " + role);
        System.out.println("=============================");

        if ("STUDENT".equals(role)) {
            return repository.findByStudentId(userId);
        }

        if ("LECTURER".equals(role) || "ADMIN".equals(role)) {
            return repository.findAll();
        }

        throw new ResponseStatusException(
                HttpStatus.FORBIDDEN,
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

        // ===== BƯỚC 1: Lấy thông tin từ JWT =====
        Long authenticatedUserId = authenticatedUserId(jwt);
        String role = jwt.getClaimAsString("role");

        // Log để debug (xem Console IntelliJ)
        System.out.println("=== DEBUG CREATE REGISTRATION ===");
        System.out.println("Token userId  = " + authenticatedUserId);
        System.out.println("Token role    = " + role);
        System.out.println("Body studentId = " + registration.getStudentId());
        System.out.println("Body topicId   = " + registration.getTopicId());
        System.out.println("=================================");

        // ===== BƯỚC 2: Chỉ STUDENT được đăng ký =====
        if (!"STUDENT".equals(role)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Chỉ sinh viên được đăng ký đề tài");
        }

        // ===== BƯỚC 3: IDOR CHECK =====
        // Nếu body có studentId khác với token → chặn ngay
        if (registration.getStudentId() != null
                && !authenticatedUserId.equals(registration.getStudentId())) {
            System.out.println(">>> IDOR DETECTED: token=" + authenticatedUserId
                    + ", body=" + registration.getStudentId());
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Không thể đăng ký đề tài cho sinh viên khác");
        }

        if (registration.getTopicId() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "topicId không được để trống");
        }

        // ===== BƯỚC 4: Luôn dùng userId từ token =====
        registration.setStudentId(authenticatedUserId);

        // ===== BƯỚC 5: Verify user qua Auth Service =====
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

        // ===== BƯỚC 6: Kiểm tra Topic =====
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

        if (topic.getCurrentStudents() >= topic.getMaxStudents()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Đề tài đã đủ số lượng sinh viên");
        }

        // ===== BƯỚC 7: Chống đăng ký trùng =====
        if (repository.existsByStudentIdAndTopicId(
                registration.getStudentId(),
                registration.getTopicId())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Sinh viên đã đăng ký đề tài này rồi");
        }

        // ===== BƯỚC 8: Lưu =====
        registration.setId(null);
        registration.setStatus("PENDING");
        registration.setCapacityVersion(0);

        return repository.save(registration);
    }

    private Long authenticatedUserId(Jwt jwt) {
        if (jwt == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                    "Thiếu JWT hợp lệ");
        }
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

        String normalizedStatus = status.trim().toUpperCase(Locale.ROOT);

        if (!ALLOWED_STATUSES.contains(normalizedStatus)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Status chỉ được phép là PENDING, APPROVED hoặc REJECTED");
        }

        Registration registration = findForUpdate(id);
        long version = registration.getCapacityVersion();

        if (!normalizedStatus.equals(registration.getStatus())
                || ("APPROVED".equals(normalizedStatus) && version == 0)) {
            version = nextVersion(version);
        }

        synchronizeCapacity(registration, normalizedStatus, version, authorization);

        registration.setStatus(normalizedStatus);
        registration.setCapacityVersion(version);
        Registration saved = repository.saveAndFlush(registration);

        // ===== THÊM LOGIC MỚI: AUTO-REJECT PENDING KHÁC =====
        if ("APPROVED".equals(normalizedStatus)) {
            autoRejectOtherPendingRegistrations(saved);
        }

        return saved;
    }

    /**
     * Khi 1 đăng ký được duyệt, tự động từ chối các đăng ký PENDING khác
     * của cùng sinh viên (vì mỗi SV chỉ được duyệt 1 đề tài).
     */
    private void autoRejectOtherPendingRegistrations(Registration approvedRegistration) {
        List<Registration> otherPending = repository
                .findByStudentIdAndStatusAndIdNot(
                        approvedRegistration.getStudentId(),
                        "PENDING",
                        approvedRegistration.getId());

        if (otherPending.isEmpty()) {
            return;
        }

        System.out.println(">>> AUTO-REJECT " + otherPending.size()
                + " PENDING registrations của SV " + approvedRegistration.getStudentId());

        for (Registration pending : otherPending) {
            pending.setStatus("REJECTED");
            pending.setUpdatedAt(java.time.LocalDateTime.now());
            pending.setNote(pending.getNote() + " [Tự động từ chối: SV đã được duyệt đề tài khác]");
            repository.save(pending);
        }
    }

    @Transactional
    public void delete(Long id, String authorization) {
        Registration registration = findForUpdate(id);
        try {
            synchronizeCapacity(registration, "REJECTED",
                    nextVersion(registration.getCapacityVersion()), authorization);
        } catch (ResponseStatusException ex) {
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