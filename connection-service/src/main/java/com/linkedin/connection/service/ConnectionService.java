package com.linkedin.connection.service;

import com.linkedin.connection.dto.ConnectionResponse;
import com.linkedin.connection.dto.NetworkConnection;
import com.linkedin.connection.payload.PageResponse;

import java.util.List;

public interface ConnectionService {

    ConnectionResponse sendRequest(Long senderId, Long receiverId);

    ConnectionResponse acceptRequest(Long connectionId, Long userId);

    ConnectionResponse rejectRequest(Long connectionId, Long userId);

    PageResponse<ConnectionResponse> getReceivedRequests(Long userId, int page, int size);

    PageResponse<ConnectionResponse> getSentRequests(Long userId, int page, int size);

    PageResponse<ConnectionResponse> getConnections(Long userId, int page, int size);

    List<NetworkConnection> getNetworkConnections(Long userId);

    void removeConnection(Long connectionId, Long userId);
}

