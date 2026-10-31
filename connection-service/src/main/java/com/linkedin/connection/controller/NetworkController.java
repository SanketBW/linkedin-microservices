package com.linkedin.connection.controller;

import com.linkedin.connection.dto.NetworkConnection;
import com.linkedin.connection.service.NetworkService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/connections/network")
@RequiredArgsConstructor
public class NetworkController {

    private final NetworkService networkService;

    @GetMapping
    public ResponseEntity<List<NetworkConnection>> getNetworkConnections(@RequestParam(defaultValue = "1") int degree, Authentication auth) {

        Long userId = Long.valueOf(auth.getName());
        List<NetworkConnection> connections = networkService.usersAtDegree(userId, degree);
        return ResponseEntity.ok(connections);
    }
}

