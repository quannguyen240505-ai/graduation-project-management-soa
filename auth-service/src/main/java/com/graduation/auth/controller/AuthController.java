package com.graduation.auth.controller;
import com.graduation.auth.dto.*; import com.graduation.auth.service.AuthenticationService; import jakarta.validation.Valid; import org.springframework.http.*; import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/auth")
public class AuthController{ private final AuthenticationService service; public AuthController(AuthenticationService s){service=s;} @PostMapping("/register") ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest q){return ResponseEntity.status(HttpStatus.CREATED).body(service.registerStudent(q));} @PostMapping("/login") ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest q){return ResponseEntity.ok(service.login(q));} }
