package com.linkedin.connection.controller;

import com.linkedin.connection.dto.ConnectionResponse;
import com.linkedin.connection.dto.SendConnectionRequest;
import com.linkedin.connection.payload.ApiResponse;
import com.linkedin.connection.payload.PageResponse;
import com.linkedin.connection.service.ConnectionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/connections")
@RequiredArgsConstructor
public class ConnectionController {

    private final ConnectionService connectionService;

    @PostMapping("/request")
    public ResponseEntity<ConnectionResponse> sendRequest(@Valid @RequestBody SendConnectionRequest request, Authentication auth) {
        Long senderId = Long.valueOf(auth.getName());
        ConnectionResponse response = connectionService.sendRequest(senderId, request.getReceiverId());
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PutMapping("/{connectionId}/accept")
    public ResponseEntity<ConnectionResponse> acceptRequest(@PathVariable Long connectionId, Authentication auth) {
        Long userId = Long.valueOf(auth.getName());
        return ResponseEntity.ok(connectionService.acceptRequest(connectionId, userId));
    }

    @PutMapping("/{connectionId}/reject")
    public ResponseEntity<ConnectionResponse> rejectRequest(@PathVariable Long connectionId, Authentication auth) {
        Long userId = Long.valueOf(auth.getName());
        return ResponseEntity.ok(connectionService.rejectRequest(connectionId, userId));
    }

    @GetMapping("/requests/received")
    public ResponseEntity<PageResponse<ConnectionResponse>> getReceivedRequests(Authentication auth,
                                                                                @RequestParam(defaultValue = "0") int page,
                                                                                @RequestParam(defaultValue = "10") int size) {
        Long userId = Long.valueOf(auth.getName());
        return ResponseEntity.ok(connectionService.getReceivedRequests(userId, page, size));
    }

    @GetMapping("/requests/sent")
    public ResponseEntity<PageResponse<ConnectionResponse>> getSentRequests(Authentication auth,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Long userId = Long.valueOf(auth.getName());
        return ResponseEntity.ok(connectionService.getSentRequests(userId, page, size));
    }

    @GetMapping
    public ResponseEntity<PageResponse<ConnectionResponse>> getConnections(Authentication auth,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Long userId = Long.valueOf(auth.getName());
        return ResponseEntity.ok(connectionService.getConnections(userId, page, size));
    }

    @DeleteMapping("/{connectionId}")
    public ResponseEntity<ApiResponse> removeConnection(@PathVariable Long connectionId, Authentication auth) {
        Long userId = Long.valueOf(auth.getName());
        connectionService.removeConnection(connectionId, userId);
        ApiResponse apiResponse = ApiResponse.builder().message("Connection removed successfully").success(true).build();
        return new ResponseEntity<>(apiResponse, HttpStatus.NO_CONTENT);
    }
}
