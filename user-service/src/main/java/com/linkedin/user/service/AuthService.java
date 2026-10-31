package com.linkedin.user.service;

import com.linkedin.user.dto.AuthResponse;
import com.linkedin.user.dto.LoginRequest;

public interface AuthService {
    AuthResponse login(LoginRequest request);
    void logout(String token);
}