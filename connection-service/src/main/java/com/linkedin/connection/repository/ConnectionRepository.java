package com.linkedin.connection.repository;

import com.linkedin.connection.dto.NetworkConnection;
import com.linkedin.connection.entity.Connection;
import com.linkedin.connection.enums.ConnectionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ConnectionRepository
        extends JpaRepository<Connection, Long> {

    // Find a relationship in either direction
    @Query("""
        SELECT c FROM Connection c
        WHERE (c.senderId = :userA AND c.receiverId = :userB)
           OR (c.senderId = :userB AND c.receiverId = :userA)
        """)
    Optional<Connection> findBetweenUsers(
            @Param("userA") Long userA,
            @Param("userB") Long userB
    );

    // Received connection requests
    Page<Connection> findByReceiverIdAndStatus(
            Long receiverId,
            ConnectionStatus status,
            Pageable pageable
    );

    // Sent connection requests
    Page<Connection> findBySenderIdAndStatus(
            Long senderId,
            ConnectionStatus status,
            Pageable pageable
    );

    // Accepted connections with pagination
    @Query(
            value = """
            SELECT c FROM Connection c
            WHERE c.status = :status
              AND (c.senderId = :userId OR c.receiverId = :userId)
            """,
            countQuery = """
            SELECT COUNT(c) FROM Connection c
            WHERE c.status = :status
              AND (c.senderId = :userId OR c.receiverId = :userId)
            """
    )
    Page<Connection> findConnectionsForUser(
            @Param("userId") Long userId,
            @Param("status") ConnectionStatus status,
            Pageable pageable
    );

    @Query(value = """
    WITH RECURSIVE network_walk(user_id, degree) AS (
        SELECT CASE WHEN sender_id = :userId THEN receiver_id ELSE sender_id END, 1
        FROM connections
        WHERE status = 'ACCEPTED'
          AND (sender_id = :userId OR receiver_id = :userId)
      UNION
        SELECT CASE WHEN c.sender_id = nw.user_id THEN c.receiver_id ELSE c.sender_id END,
               nw.degree + 1
        FROM network_walk nw
        JOIN connections c
          ON c.status = 'ACCEPTED'
         AND (c.sender_id = nw.user_id OR c.receiver_id = nw.user_id)
        WHERE nw.degree < 3
    )
    SELECT user_id AS "userId", MIN(degree) AS "degree"
    FROM network_walk
    WHERE user_id <> :userId
    GROUP BY user_id
    ORDER BY MIN(degree), user_id
    """, nativeQuery = true)
    List<NetworkConnection> findNetworkConnections(@Param("userId") Long userId);
}
