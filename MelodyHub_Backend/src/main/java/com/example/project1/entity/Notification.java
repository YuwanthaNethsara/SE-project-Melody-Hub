package com.example.project1.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/** An in-app notification created by an Observer when music content changes. */
@Entity
@Table(name = "notifications")
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long notificationId;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 40)
    private String type;

    @Column(nullable = false, length = 500)
    private String message;

    private Long artistId;

    @Column(nullable = false)
    private boolean seen;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    public Notification() {
    }

    public Notification(User user, String type, String message, Long artistId) {
        this.user = user;
        this.type = type;
        this.message = message;
        this.artistId = artistId;
        this.seen = false;
        this.createdAt = LocalDateTime.now();
    }

    public Long getNotificationId() { return notificationId; }
    public User getUser() { return user; }
    public String getType() { return type; }
    public String getMessage() { return message; }
    public Long getArtistId() { return artistId; }
    public boolean isSeen() { return seen; }
    public void setSeen(boolean seen) { this.seen = seen; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
