package com.enviro.assessment.junior.bonganimoche.service;

import com.enviro.assessment.junior.bonganimoche.dto.request.LoginRequest;
import com.enviro.assessment.junior.bonganimoche.dto.response.AuthResponse;

public interface AuthService {
    AuthResponse login(LoginRequest request);
}