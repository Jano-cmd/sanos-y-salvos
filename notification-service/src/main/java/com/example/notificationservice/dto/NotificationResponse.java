package com.example.notificationservice.dto;

import com.example.notificationservice.model.Notification;

import java.time.Instant;
import java.util.List;

public class NotificationResponse {

    private Long id;
    private Long reportId;
    private String type;
    private String description;
    private Instant createdAt;
    private Double lat;
    private Double lng;
    private List<String> imageUrls;

    public static NotificationResponse from(Notification notification) {
        NotificationResponse response = new NotificationResponse();
        response.setId(notification.getId());
        response.setReportId(notification.getReportId());
        response.setType(notification.getType());
        response.setDescription(notification.getDescription());
        response.setCreatedAt(notification.getCreatedAt());
        response.setLat(notification.getLat());
        response.setLng(notification.getLng());
        response.setImageUrls(notification.getImageUrls());
        return response;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getReportId() {
        return reportId;
    }

    public void setReportId(Long reportId) {
        this.reportId = reportId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Double getLat() {
        return lat;
    }

    public void setLat(Double lat) {
        this.lat = lat;
    }

    public Double getLng() {
        return lng;
    }

    public void setLng(Double lng) {
        this.lng = lng;
    }

    public List<String> getImageUrls() {
        return imageUrls;
    }

    public void setImageUrls(List<String> imageUrls) {
        this.imageUrls = imageUrls;
    }
}
