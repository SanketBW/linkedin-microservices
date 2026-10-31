
package com.linkedin.user.service.impl;

import com.linkedin.user.dto.RegisterRequest;
import com.linkedin.user.dto.UpdateProfileRequest;
import com.linkedin.user.dto.UserResponse;
import com.linkedin.user.entity.User;
import com.linkedin.user.exception.EmailAlreadyExistsException;
import com.linkedin.user.exception.ResourceNotFoundException;
import com.linkedin.user.repository.UserRepository;
import com.linkedin.user.service.UserService;

import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ModelMapper modelMapper;

    @Override
    public UserResponse registerUser(RegisterRequest request) {

        String email = request.getEmail().trim().toLowerCase();

        if (userRepository.existsByEmailIgnoreCase(email))
            throw new EmailAlreadyExistsException("Email is already registered!");

        User user = new User();
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        User savedUser = userRepository.save(user);
        return this.modelMapper.map(savedUser, UserResponse.class);
    }

    @Override
    public List<UserResponse> getAllUser() {
        List<User> users = this.userRepository.findAll();
        return users.stream().map((user -> this.modelMapper.map(user, UserResponse.class))).toList();
    }

    @Override
    public UserResponse getUserByEmail(String email) {
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return this.modelMapper.map(user, UserResponse.class);
    }

    @Override
    public UserResponse updateMyProfile(String email, UpdateProfileRequest request) {
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (request.getFirstName() != null) {
            user.setFirstName(request.getFirstName());
        }

        if (request.getLastName() != null) {
            user.setLastName(request.getLastName());
        }

        if (request.getHeadline() != null) {
            user.setHeadline(request.getHeadline());
        }

        if (request.getAbout() != null) {
            user.setAbout(request.getAbout());
        }

        User updatedUser = userRepository.save(user);

        return this.modelMapper.map(updatedUser, UserResponse.class);
    }
}
