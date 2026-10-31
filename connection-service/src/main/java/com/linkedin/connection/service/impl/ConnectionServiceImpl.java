package com.linkedin.connection.service.impl;

import com.linkedin.connection.dto.ConnectionResponse;
import com.linkedin.connection.dto.NetworkConnection;
import com.linkedin.connection.entity.Connection;
import com.linkedin.connection.enums.ConnectionEventType;
import com.linkedin.connection.enums.ConnectionStatus;
import com.linkedin.connection.event.ConnectionEvent;
import com.linkedin.connection.exception.BadRequestException;
import com.linkedin.connection.exception.ConflictException;
import com.linkedin.connection.exception.ForbiddenOperationException;
import com.linkedin.connection.exception.ResourceNotFoundException;
import com.linkedin.connection.payload.PageResponse;
import com.linkedin.connection.producer.ConnectionEventProducer;
import com.linkedin.connection.repository.ConnectionRepository;
import com.linkedin.connection.service.ConnectionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ConnectionServiceImpl implements ConnectionService {

    private final ConnectionRepository connectionRepository;
    private final ConnectionEventProducer connectionEventProducer;

    @Override
    public ConnectionResponse sendRequest(Long senderId, Long receiverId) {
        validateUserId(senderId, "Sender ID");
        validateUserId(receiverId, "Receiver ID");
        if (senderId.equals(receiverId))
            throw new BadRequestException("You cannot send a connection request to yourself");

        Connection connection = connectionRepository.findBetweenUsers(senderId, receiverId)
                .map(existing -> {
                    switch (existing.getStatus()) {
                        case ACCEPTED ->
                                throw new ConflictException("You are already connected with this user");
                        case PENDING ->
                                throw new ConflictException(existing.getSenderId().equals(senderId)
                                                ? "You have already sent a connection request"
                                                : "This user has already sent you a connection request");
                        case REJECTED -> {
                            // Reuse the rejected row for the new request.
                            existing.setSenderId(senderId);
                            existing.setReceiverId(receiverId);
                            existing.setStatus(ConnectionStatus.PENDING);
                        }
                    }
                    return existing;
                })
                .orElseGet(() -> {
                    Connection newConnection = new Connection();
                    newConnection.setSenderId(senderId);
                    newConnection.setReceiverId(receiverId);
                    newConnection.setStatus(ConnectionStatus.PENDING);
                    return newConnection;
                });

        Connection saved = connectionRepository.save(connection);

        connectionEventProducer.publish(
                new ConnectionEvent(
                        UUID.randomUUID().toString(),
                        ConnectionEventType.REQUESTED,
                        saved.getId(),
                        senderId,
                        receiverId,
                        LocalDateTime.now()));
        log.info("Connection request id={} from={} to={}", saved.getId(), senderId, receiverId);
        return toResponse(saved);
    }

    @Override
    public ConnectionResponse acceptRequest(Long connectionId, Long userId) {
        validateUserId(userId, "User ID");
        Connection connection = getConnection(connectionId);
        verifyReceiver(connection, userId);
        requirePending(connection, "accepted");
        connection.setStatus(ConnectionStatus.ACCEPTED);
        Connection saved = connectionRepository.save(connection);
        connectionEventProducer.publish(
                new ConnectionEvent(
                        UUID.randomUUID().toString(),
                        ConnectionEventType.ACCEPTED,
                        saved.getId(),
                        userId,
                        saved.getSenderId(),
                        LocalDateTime.now()));

        log.info("Connection id={} accepted by userId={}", connectionId, userId);
        return toResponse(saved);
    }

    @Override
    public ConnectionResponse rejectRequest(Long connectionId, Long userId) {
        validateUserId(userId, "User ID");
        Connection connection = getConnection(connectionId);
        verifyReceiver(connection, userId);
        requirePending(connection, "rejected");
        connection.setStatus(ConnectionStatus.REJECTED);
        Connection saved = connectionRepository.save(connection);
        log.info("Connection id={} rejected by userId={}", connectionId, userId);
        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ConnectionResponse> getReceivedRequests(Long userId, int page, int size) {
        validateUserId(userId, "User ID");
        Pageable pageable = createPageable(page, size);
        Page<Connection> result = connectionRepository.findByReceiverIdAndStatus(userId, ConnectionStatus.PENDING, pageable);
        return toPageResponse(result);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ConnectionResponse> getSentRequests(Long userId, int page, int size) {
        validateUserId(userId, "User ID");
        Pageable pageable = createPageable(page, size);
        Page<Connection> result = connectionRepository.findBySenderIdAndStatus(userId, ConnectionStatus.PENDING, pageable);
        return toPageResponse(result);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ConnectionResponse> getConnections(Long userId, int page, int size) {
        validateUserId(userId, "User ID");
        Pageable pageable = createPageable(page, size);
        Page<Connection> result = connectionRepository.findConnectionsForUser(userId, ConnectionStatus.ACCEPTED, pageable);
        return toPageResponse(result);
    }

    @Override
    @Transactional(readOnly = true)
    public List<NetworkConnection> getNetworkConnections(Long userId) {
        validateUserId(userId, "User ID");
        return connectionRepository.findNetworkConnections(userId);
    }

    @Override
    public void removeConnection(Long connectionId, Long userId) {
        validateUserId(userId, "User ID");
        Connection connection = getConnection(connectionId);
        boolean isSender = connection.getSenderId().equals(userId);
        boolean isReceiver = connection.getReceiverId().equals(userId);

        if (!isSender && !isReceiver)
            throw new ForbiddenOperationException("You are not allowed to remove this connection");

        switch (connection.getStatus()) {
            case ACCEPTED -> {
                // Either participant can remove an accepted connection.
            }
            case PENDING -> {
                if (!isSender)
                    throw new ForbiddenOperationException("Only the sender can withdraw a pending request");
            }
            case REJECTED ->
                    throw new ConflictException("Rejected requests cannot be removed through this operation");
        }
        connectionRepository.delete(connection);
        log.info("Connection id={} removed by userId={}", connectionId, userId);
    }

    private Connection getConnection(Long connectionId) {
        validateUserId(connectionId, "Connection ID");
        return connectionRepository.findById(connectionId)
                .orElseThrow(() -> new ResourceNotFoundException("Connection not found with ID: " + connectionId));
    }

    private void verifyReceiver(Connection connection, Long userId) {
        if (!connection.getReceiverId().equals(userId))
            throw new ForbiddenOperationException("Only the receiver can perform this action");
    }

    private void requirePending(Connection connection, String action) {
        if (connection.getStatus() != ConnectionStatus.PENDING)
            throw new ConflictException("Only pending requests can be " + action);
    }

    private void validateUserId(Long id, String fieldName) {
        if (id == null || id <= 0)
            throw new BadRequestException(fieldName + " must be greater than zero");
    }

    private Pageable createPageable(int page, int size) {
        if (page < 0)
            throw new BadRequestException("Page number cannot be negative");

        if (size < 1 || size > 100)
            throw new BadRequestException("Page size must be between 1 and 100");

        return PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
    }

    private PageResponse<ConnectionResponse> toPageResponse(Page<Connection> page) {

        return PageResponse.<ConnectionResponse>builder()
                .content(page.getContent()
                                .stream()
                                .map(this::toResponse)
                                .toList())
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .build();
    }

    private ConnectionResponse toResponse(Connection connection) {
        return ConnectionResponse.builder()
                .id(connection.getId())
                .senderId(connection.getSenderId())
                .receiverId(connection.getReceiverId())
                .status(connection.getStatus())
                .createdAt(connection.getCreatedAt())
                .updatedAt(connection.getUpdatedAt())
                .build();
    }
}
