package com.example.notificationservice.service;

import com.example.notificationservice.dto.CreateNotificationRequest;
import com.example.notificationservice.model.Notification;
import com.example.notificationservice.repository.NotificationRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    public List<Notification> getAllNotifications() {
        return notificationRepository.findAll();
    }

    public Optional<Notification> getNotificationById(Long id) {
        return notificationRepository.findById(id);
    }

    public Notification createNotification(CreateNotificationRequest request) {
        validate(request);

        Notification notification = new Notification();
        notification.setReportId(request.getReportId());
        notification.setType(request.getType());
        notification.setDescription(request.getDescription());
        notification.setCreatedAt(request.getCreatedAt());
        notification.setLat(request.getLat());
        notification.setLng(request.getLng());
        notification.setImageUrls(request.getImageUrls());
        return notificationRepository.save(notification);
    }

    private void validate(CreateNotificationRequest request) {
        if (request.getReportId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "reportId is required");
        }
        if (request.getType() == null || request.getType().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "type is required");
        }
        if (request.getDescription() == null || request.getDescription().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "description is required");
        }
        if (request.getCreatedAt() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "createdAt is required");
        }
        if (request.getLat() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "lat is required");
        }
        if (request.getLng() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "lng is required");
        }
    }
}
