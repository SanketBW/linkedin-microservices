package com.linkedin.notification.service;

import com.linkedin.notification.dto.NotificationResponse;
import com.linkedin.notification.entity.Notification;
import com.linkedin.notification.enums.NotificationType;
import com.linkedin.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.errors.ResourceNotFoundException;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class NotificationService {

    private final NotificationRepository repository;
    private final ModelMapper modelMapper;

    public void record(String eventId, Long recipientId, Long actorId, NotificationType type, Long referenceId, LocalDateTime createdAt)
    {
        if (eventId == null || recipientId == null || actorId == null || type == null) {
            throw new IllegalArgumentException("Invalid notification event data");
        }
        // prevent self notification
        if (recipientId.equals(actorId)) {
            return;
        }

        // Prevent duplicate notifications
        if (repository.existsByEventId(eventId)) {
            log.info("Duplicate event ignored: {}", eventId);
            return;
        }

        Notification notification = new Notification();

        notification.setEventId(eventId);
        notification.setRecipientId(recipientId);
        notification.setActorId(actorId);
        notification.setType(type);
        notification.setReferenceId(referenceId);
        notification.setCreatedAt(createdAt);

        repository.save(notification);

        log.info("Notification saved: type={}, recipientId={}", type, recipientId);
    }

    @Transactional
    public void deleteAllNotificationsOf(Long userId) {

        repository.deleteByRecipientIdOrActorId(userId, userId);
        log.info("Deleted notifications for userId={}", userId);
    }

    @Transactional(readOnly = true)
    public Page<NotificationResponse> getNotifications(Long recipientId, Pageable pageable) {
        Pageable safePageable = PageRequest.of(pageable.getPageNumber(), Math.min(pageable.getPageSize(), 100));
        return repository.findAllByRecipientIdOrderByCreatedAtDesc(recipientId, safePageable)
                .map(notification -> modelMapper.map(notification, NotificationResponse.class));
    }

    @Transactional
    public void deleteNotification(Long notificationId, Long recipientId){
        Notification notification = repository.findByIdAndRecipientId(notificationId, recipientId)
                .orElseThrow(()-> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Notification not found with id: " + notificationId));

        repository.delete(notification);
    }

    @Transactional
    public void markAsRead(Long notificationId){
        Notification notification = repository.findById(notificationId).
                orElseThrow(()-> new ResourceNotFoundException("Notification Not Found!" + notificationId));
        notification.setRead(true);
        repository.save(notification);
    }

}