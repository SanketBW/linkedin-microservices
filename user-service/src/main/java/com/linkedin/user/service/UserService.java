package com.linkedin.user.service;

import com.linkedin.user.dto.RegisterRequest;
import com.linkedin.user.dto.UpdateProfileRequest;
import com.linkedin.user.dto.UserResponse;

import java.util.List;

public interface UserService {

    UserResponse registerUser(RegisterRequest request);

    List<UserResponse> getAllUser();

    UserResponse getUserByEmail(String email);

    UserResponse updateMyProfile(String email, UpdateProfileRequest request);
}

