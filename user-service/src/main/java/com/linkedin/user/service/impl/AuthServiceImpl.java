package com.linkedin.user.service.impl;

import com.linkedin.user.dto.AuthResponse;
import com.linkedin.user.dto.LoginRequest;
import com.linkedin.user.entity.User;
import com.linkedin.user.exception.InvalidCredentialsException;
import com.linkedin.user.repository.UserRepository;
import com.linkedin.user.security.TokenBlackListService;
import com.linkedin.user.service.AuthService;
import com.linkedin.user.security.JwtService;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final TokenBlackListService blacklistService;

    @Override
    public AuthResponse login(LoginRequest request) {

        User user = userRepository.findByEmailIgnoreCase(request.getEmail().trim())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword()))
            throw new InvalidCredentialsException("Invalid email or password");

        String accessToken = jwtService.generateJwtToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);
        return new AuthResponse(accessToken, refreshToken);
    }

    @Override
    public void logout(String token) {
        blacklistService.add(token);
    }
}

