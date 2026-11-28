package com.linkedin.connection.repository;

import com.linkedin.connection.entity.Connection;
import com.linkedin.connection.enums.ConnectionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ConnectionRepository extends JpaRepository<Connection, Long> {

    // Find an existing connection in either direction
    @Query("""
        SELECT c FROM Connection c
        WHERE (c.senderId = :userId AND c.receiverId = :otherUserId)
           OR (c.senderId = :otherUserId AND c.receiverId = :userId)
        """)
    Optional<Connection> findBetweenUsers(@Param("userId") Long userId, @Param("otherUserId") Long otherUserId);

    // Get received pending requests
    Page<Connection> findByReceiverIdAndStatus(Long receiverId, ConnectionStatus status, Pageable pageable);

    // Get sent pending requests
    Page<Connection> findBySenderIdAndStatus(Long senderId, ConnectionStatus status, Pageable pageable);

    // Get accepted connections
    @Query("""
    SELECT c FROM Connection c
    WHERE c.status = :status
    AND (c.senderId = :userId OR c.receiverId = :userId)
    """)
    Page<Connection> findConnectionsForUser(@Param("userId") Long userId, @Param("status") ConnectionStatus status, Pageable pageable);

    void deleteBySenderIdOrReceiverId(Long senderId, Long receiverId);
}
