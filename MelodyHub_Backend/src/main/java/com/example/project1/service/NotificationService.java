package com.example.project1.service;

import com.example.project1.dto.ArtistResponse;
import com.example.project1.dto.NotificationResponse;

import java.util.List;

/** Follow / unfollow artists (attach / detach) and read the notifications the observers created. */
public interface NotificationService {

    String follow(String email, Long artistId);

    String unfollow(String email, Long artistId);

    List<ArtistResponse> getFollowedArtists(String email);

    List<NotificationResponse> getMyNotifications(String email);

    long countUnread(String email);

    NotificationResponse markAsRead(String email, Long notificationId);

    int markAllAsRead(String email);
}
