package com.linkedin.user.controller;

import com.linkedin.user.dto.RegisterRequest;
import com.linkedin.user.dto.UpdateProfileRequest;
import com.linkedin.user.dto.UserResponse;
import com.linkedin.user.service.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<UserResponse> registerUser(@Valid @RequestBody RegisterRequest request) {
        UserResponse response = userService.registerUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getMyProfile(Authentication auth) {
        UserResponse response = userService.getUserByEmail(auth.getName());
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<UserResponse>> getAllUser(){
        List<UserResponse> response = userService.getAllUser();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PutMapping("/me")
    public ResponseEntity<UserResponse> updateMyProfile(Authentication auth, @Valid @RequestBody UpdateProfileRequest request) {
        UserResponse response = userService.updateMyProfile(auth.getName(), request);
        return ResponseEntity.ok(response);
    }
}
