package com.linkedin.user.controller;

import com.linkedin.user.dto.AuthResponse;
import com.linkedin.user.dto.LoginRequest;
import com.linkedin.user.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> userLogin(@RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(@RequestHeader("Authorization") String header) {
        if (header == null || !header.startsWith("Bearer ") || header.length() <= 7)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid or missing Bearer token");
        authService.logout(header.substring(7));
        return ResponseEntity.ok("Logged out successfully");
    }
}
