package com.graduation.topic.controller;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.graduation.topic.dto.TopicRequest;
import com.graduation.topic.dto.TopicResponse;
import com.graduation.topic.service.TopicService;

@RestController
@RequestMapping("/api/topics")
public class TopicController {

    private final TopicService service;

    public TopicController(TopicService service) {
        this.service = service;
    }

    @GetMapping
    public List<TopicResponse> getAll() {
        return service.getAll();
    }

    @GetMapping("/{id}")
    public TopicResponse getById(@PathVariable("id") Long id) {
        return service.getById(id);
    }

    @PostMapping
    public ResponseEntity<TopicResponse> create(
            @Valid @RequestBody TopicRequest request,
            @AuthenticationPrincipal Jwt jwt) {

        TopicResponse response = service.create(
                request,
                jwt.getTokenValue());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TopicResponse> update(
            @PathVariable("id") Long id,
            @Valid @RequestBody TopicRequest request,
            @AuthenticationPrincipal Jwt jwt) {

        TopicResponse response = service.update(
                id,
                request,
                jwt.getTokenValue());

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable("id") Long id) {

        service.delete(id);

        return ResponseEntity.noContent().build();
    }
}	