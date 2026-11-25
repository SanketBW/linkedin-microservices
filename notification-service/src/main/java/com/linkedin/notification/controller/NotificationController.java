package com.linkedin.notification.controller;

import com.linkedin.notification.dto.NotificationResponse;
import com.linkedin.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notification")
@RequiredArgsConstructor
public class NotificationController {
    private final NotificationService service;

    @GetMapping("/me")
    public ResponseEntity<Page<NotificationResponse>> getAllNotifications(Authentication auth,
                                                                    @RequestParam(defaultValue = "0") int page,
                                                                    @RequestParam(defaultValue ="10") int size){
        Long recipientId = Long.valueOf(auth.getName());
        Pageable pageable = PageRequest.of(page, size);
        Page<NotificationResponse> response = service.getNotifications(recipientId, pageable);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PatchMapping("/{notificationId}/read")
    public void markNotificationAsRead(@PathVariable("notificationId") Long id){
        service.markAsRead(id);
    }

    @DeleteMapping("/{notificationId}")
    public ResponseEntity<Void> deleteNotification(@PathVariable Long notificationId , Authentication auth){
        Long recipientId = Long.valueOf(auth.getName());
        service.deleteNotification(notificationId, recipientId);
        return ResponseEntity.noContent().build();
    }

}
