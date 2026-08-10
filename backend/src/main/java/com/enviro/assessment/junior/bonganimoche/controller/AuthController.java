package com.enviro.assessment.junior.bonganimoche.controller;

import com.enviro.assessment.junior.bonganimoche.dto.request.LoginRequest;
import com.enviro.assessment.junior.bonganimoche.dto.response.AuthResponse;
import com.enviro.assessment.junior.bonganimoche.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /** POST /api/auth/login */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }
}