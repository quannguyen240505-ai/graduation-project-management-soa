package com.group.registration.controller;

import com.group.registration.entity.Registration;
import com.group.registration.service.RegistrationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/registrations")
public class RegistrationController {
    private final RegistrationService service;

    public RegistrationController(RegistrationService service) {
        this.service = service;
    }

    // GET /api/registrations - Lấy tất cả
    @GetMapping
    public List<Registration> getAll() {
        return service.getAll();
    }

    // GET /api/registrations/{id} - Lấy chi tiết
    @GetMapping("/{id}")
    public Registration getById(@PathVariable Long id) {
        return service.getById(id);
    }

    // POST /api/registrations - Tạo mới
    @PostMapping
    public Registration create(@RequestBody Registration registration) {
        return service.create(registration);
    }

    // PUT /api/registrations/{id}/status - Cập nhật trạng thái
    @PutMapping("/{id}/status")
    public Registration updateStatus(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String status = body.get("status");
        return service.updateStatus(id, status);
    }

    // DELETE /api/registrations/{id} - Xóa
    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.ok("Đã xóa đăng ký với id = " + id);
    }
}