package com.example.project1.controller;

import com.example.project1.dto.ArtistResponse;
import com.example.project1.dto.NotificationResponse;
import com.example.project1.service.NotificationService;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Observer pattern - the "attach / detach" and "update received" ends of the relationship:
 * a logged-in user follows artists and reads the notifications the observers created for them.
 * Everything is scoped to the logged-in user (the email comes from the JWT, never from the request).
 */
@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping("/me")
    public ResponseEntity<List<NotificationResponse>> myNotifications(Authentication authentication) {
        return ResponseEntity.ok(notificationService.getMyNotifications(authentication.getName()));
    }

    @GetMapping("/me/unread-count")
    public ResponseEntity<Map<String, Long>> unreadCount(Authentication authentication) {
        return ResponseEntity.ok(Map.of("unread", notificationService.countUnread(authentication.getName())));
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<NotificationResponse> markAsRead(Authentication authentication, @PathVariable Long id) {
        return ResponseEntity.ok(notificationService.markAsRead(authentication.getName(), id));
    }

    @PutMapping("/read-all")
    public ResponseEntity<Map<String, Integer>> markAllAsRead(Authentication authentication) {
        return ResponseEntity.ok(Map.of("updated", notificationService.markAllAsRead(authentication.getName())));
    }

    @GetMapping("/following")
    public ResponseEntity<List<ArtistResponse>> following(Authentication authentication) {
        return ResponseEntity.ok(notificationService.getFollowedArtists(authentication.getName()));
    }

    @PostMapping("/follow/{artistId}")
    public ResponseEntity<Map<String, String>> follow(Authentication authentication, @PathVariable Long artistId) {
        return ResponseEntity.ok(Map.of("message", notificationService.follow(authentication.getName(), artistId)));
    }

    @DeleteMapping("/follow/{artistId}")
    public ResponseEntity<Map<String, String>> unfollow(Authentication authentication, @PathVariable Long artistId) {
        return ResponseEntity.ok(Map.of("message", notificationService.unfollow(authentication.getName(), artistId)));
    }
}
