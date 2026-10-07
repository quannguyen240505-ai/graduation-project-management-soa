package com.group.registration.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.group.registration.entity.Registration;
import com.group.registration.service.RegistrationService;

@RestController
@RequestMapping("/api/registrations")
public class RegistrationController {

    private final RegistrationService service;

    public RegistrationController(
            RegistrationService service) {

        this.service = service;
    }

    @GetMapping
    public List<Registration> getAll() {
        return service.getAll();
    }

    @GetMapping("/{id}")
    public Registration getById(
            @PathVariable Long id) {

        return service.getById(id);
    }

    @PostMapping
    public ResponseEntity<Registration> create(
            @RequestBody Registration registration,
            @RequestHeader(HttpHeaders.AUTHORIZATION)
            String authorization) {

        Registration created =
                service.create(
                        registration,
                        authorization);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(created);
    }

    @PutMapping("/{id}/status")
    public Registration updateStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {

        return service.updateStatus(
                id,
                body.get("status"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id) {

        service.delete(id);

        return ResponseEntity
                .noContent()
                .build();
    }
}