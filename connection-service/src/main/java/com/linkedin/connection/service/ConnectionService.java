package com.linkedin.connection.service;

import com.linkedin.connection.dto.ConnectionResponse;
import com.linkedin.connection.payload.PageResponse;
import org.springframework.data.domain.Pageable;

public interface ConnectionService {

    ConnectionResponse sendRequest(Long senderId, Long receiverId, String token);

    ConnectionResponse acceptRequest(Long connectionId, Long userId);

    ConnectionResponse rejectRequest(Long connectionId, Long userId);

    PageResponse<ConnectionResponse> getReceivedRequests(Long userId, Pageable pageable);

    PageResponse<ConnectionResponse> getSentRequests(Long userId, Pageable pageable);

    PageResponse<ConnectionResponse> getConnections(Long userId, Pageable pageable);

    void removeConnection(Long connectionId, Long userId);
}

