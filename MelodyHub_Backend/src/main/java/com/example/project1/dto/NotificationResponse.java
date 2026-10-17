package com.example.project1.dto;

import java.time.LocalDateTime;

public class NotificationResponse {

    private Long notificationId;
    private String type;
    private String message;
    private Long artistId;
    private boolean seen;
    private LocalDateTime createdAt;

    public NotificationResponse() {
    }

    public NotificationResponse(Long notificationId, String type, String message,
                                Long artistId, boolean seen, LocalDateTime createdAt) {
        this.notificationId = notificationId;
        this.type = type;
        this.message = message;
        this.artistId = artistId;
        this.seen = seen;
        this.createdAt = createdAt;
    }

    public Long getNotificationId() { return notificationId; }
    public String getType() { return type; }
    public String getMessage() { return message; }
    public Long getArtistId() { return artistId; }
    public boolean isSeen() { return seen; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
