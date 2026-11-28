package com.linkedin.connection.service.impl;

import com.linkedin.connection.client.UserClient;
import com.linkedin.connection.dto.ConnectionResponse;
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
import feign.FeignException;
import lombok.RequiredArgsConstructor;

import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class ConnectionServiceImpl implements ConnectionService {

    private final ConnectionRepository connectionRepository;
    private final ModelMapper modelMapper;
    private final UserClient userClient;
    private final ConnectionEventProducer connectionEventProducer;

    private static final int MAX_PAGE_SIZE = 100;

    // Send connection request
    @Override
    public ConnectionResponse sendRequest(Long senderId, Long receiverId, String token) {

        validateUserId(senderId, "Sender ID");
        validateUserId(receiverId, "Receiver ID");


        if (senderId.equals(receiverId))
            throw new BadRequestException("You cannot send a connection request to yourself");

        validateUserExists(senderId, "Sender user not found", token);
        validateUserExists(receiverId, "Receiver user not found", token);

        Connection connection = connectionRepository
                .findBetweenUsers(senderId, receiverId)
                .map(existing -> {
                    if (existing.getStatus() == ConnectionStatus.ACCEPTED)
                        throw new ConflictException("You are already connected with this user");

                    if (existing.getStatus() == ConnectionStatus.PENDING)
                        throw new ConflictException("A connection request already exists");

                    // Reuse rejected request :
                    //Sender: User A (10)
                    //Receiver: User B (20)
                    //
                    //PENDING
                    //
                    //User B rejects the request.
                    //REJECTED
                    //
                    //Now, User A wants to send another connection request to User B.
                    //
                    //Your code finds the existing rejected db record:
                    existing.setSenderId(senderId);
                    existing.setReceiverId(receiverId);
                    existing.setStatus(ConnectionStatus.PENDING);

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

        // Publish kafka event : Request Sent
        ConnectionEvent event = new ConnectionEvent(
                UUID.randomUUID().toString(),
                ConnectionEventType.REQUESTED,
                saved.getId(),
                senderId,
                receiverId,
                LocalDateTime.now()
        );

        connectionEventProducer.publish(event);
        return this.modelMapper.map(saved, ConnectionResponse.class);
    }

    // Accept connection request
    @Override
    public ConnectionResponse acceptRequest(Long connectionId, Long userId) {

        validateUserId(connectionId, "Connection ID");

        Connection connection = connectionRepository.findById(connectionId)
                .orElseThrow(() -> new ResourceNotFoundException("Connection not found with ID: " + connectionId));;

        verifyReceiver(connection, userId);
        requirePending(connection);

        connection.setStatus(ConnectionStatus.ACCEPTED);

        // Publish kafka event : Accept Request
        ConnectionEvent event = new ConnectionEvent(
                UUID.randomUUID().toString(),
                ConnectionEventType.ACCEPTED,
                connection.getId(),
                userId,
                connection.getSenderId(),
                LocalDateTime.now()
                );

        connectionEventProducer.publish(event);
        return this.modelMapper.map(connection, ConnectionResponse.class);
    }

    // Reject connection request
    @Override
    public ConnectionResponse rejectRequest(Long connectionId, Long userId) {

        validateUserId(userId, "User Id");
        validateUserId(connectionId, "Connection Id");
        Connection connection = connectionRepository.findById(connectionId)
                .orElseThrow(() -> new ResourceNotFoundException("Connection not found with ID: " + connectionId));;

        verifyReceiver(connection, userId);
        requirePending(connection);

        connection.setStatus(ConnectionStatus.REJECTED);
        return this.modelMapper.map(connection, ConnectionResponse.class);
    }

    // Get received requests
    @Override
    @Transactional(readOnly = true)
    public PageResponse<ConnectionResponse> getReceivedRequests(Long userId, Pageable pageable) {

        validateUserId(userId, "User ID");
        Page<Connection> result =
                connectionRepository.findByReceiverIdAndStatus(userId, ConnectionStatus.PENDING, validatePageable(pageable));

        return new PageResponse<>(
                result.getContent().stream().
                        map((element) -> modelMapper.map(element, ConnectionResponse.class)).toList(),
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages(),
                result.isLast()
        );
    }

    // Get sent requests
    @Override
    @Transactional(readOnly = true)
    public PageResponse<ConnectionResponse> getSentRequests(Long userId, Pageable pageable) {

        validateUserId(userId, "User ID");
        Page<Connection> result =
                connectionRepository.findBySenderIdAndStatus(userId, ConnectionStatus.PENDING, validatePageable(pageable));

        return new PageResponse<>(
                result.getContent().stream()
                        .map((element) -> modelMapper.map(element, ConnectionResponse.class)).toList(),
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages(),
                result.isLast()
        );
    }

    // Get accepted connections
    @Override
    @Transactional(readOnly = true)
    public PageResponse<ConnectionResponse> getConnections(Long userId, Pageable pageable) {

        validateUserId(userId, "User ID");
        Page<Connection> result =
                connectionRepository.findConnectionsForUser(userId, ConnectionStatus.ACCEPTED, validatePageable(pageable));

        return new PageResponse<>(
                result.getContent().stream()
                        .map((element) -> modelMapper.map(element, ConnectionResponse.class)).toList(),
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages(),
                result.isLast()
        );
    }

    // Remove connection
    @Override
    public void removeConnection(Long connectionId, Long userId) {

        validateUserId(userId, "User Id");
        Connection connection = connectionRepository.findById(connectionId)
                .orElseThrow(() -> new ResourceNotFoundException("Connection not found with ID: " + connectionId));;

        boolean isSender = connection.getSenderId().equals(userId);
        boolean isReceiver = connection.getReceiverId().equals(userId);

        if (!isSender && !isReceiver)
            throw new ForbiddenOperationException("You are not allowed to remove this connection");

        if (connection.getStatus() == ConnectionStatus.REJECTED)
            throw new ConflictException("Rejected requests cannot be removed through this operation");

        if (connection.getStatus() == ConnectionStatus.PENDING && !isSender)
            throw new ForbiddenOperationException("Only the sender can withdraw a pending request");

        connectionRepository.delete(connection);
    }

    private void verifyReceiver(Connection connection, Long userId) {

        validateUserId(userId, "User ID");

        if (!connection.getReceiverId().equals(userId))
            throw new ForbiddenOperationException("Only the receiver can perform this action");
    }

    private void requirePending(Connection connection) {

        if (connection.getStatus() != ConnectionStatus.PENDING)
            throw new ConflictException("Only pending requests can be processed");
    }

    private void validateUserId(Long id, String fieldName) {

        if (id == null || id <= 0)
            throw new BadRequestException(fieldName + " must be greater than zero");
    }

    private Pageable validatePageable(Pageable pageable) {

        if (pageable == null || pageable.getPageSize() < 1)
            throw new BadRequestException("Invalid pagination parameters");

        Pageable safePageable = PageRequest.of(
                pageable.getPageNumber(),
                Math.min(pageable.getPageSize(), MAX_PAGE_SIZE),
                Sort.by(Sort.Direction.DESC, "createdAt"));

        return safePageable;
    }

    private void validateUserExists(Long userId, String message, String token) {
        try {
            userClient.getUserById(userId, token);
        } catch (FeignException.NotFound ex) {
            throw new ResourceNotFoundException(message);
        }
    }
}
