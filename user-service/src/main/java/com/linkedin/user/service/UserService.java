package com.linkedin.user.service;

import com.linkedin.user.dto.PageResponse;
import com.linkedin.user.dto.RegisterRequest;
import com.linkedin.user.dto.UpdateProfileRequest;
import com.linkedin.user.dto.UserResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface UserService {

    UserResponse registerUser(RegisterRequest request);

    UserResponse getUser(Long id);

    List<UserResponse> getAllUser();

    UserResponse getUserByEmail(String email);

    PageResponse searchByUser(String keyword, Pageable pageable);

    UserResponse updateMyProfile(String email, UpdateProfileRequest request);

    void deleteMyAccount(String email);
}

