package com.linkedin.connection.service;

import com.linkedin.connection.dto.NetworkConnection;
import com.linkedin.connection.exception.BadRequestException;
import com.linkedin.connection.repository.ConnectionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NetworkService {

    private final ConnectionRepository connectionRepository;

    public List<NetworkConnection> usersAtDegree(Long userId, int degree) {
        validateUserId(userId);
        if (degree < 1 || degree > 3)
            throw new BadRequestException("Degree must be between 1 and 3");

        return connectionRepository.findNetworkConnections(userId)
                .stream()
                .filter(connection -> connection.getDegree() == degree)
                .toList();
    }

    private void validateUserId(Long userId) {
        if (userId == null || userId <= 0)
            throw new BadRequestException("User ID must be greater than zero");
    }
}
