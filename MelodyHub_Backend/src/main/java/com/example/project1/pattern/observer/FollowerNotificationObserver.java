package com.example.project1.pattern.observer;

import com.example.project1.entity.ArtistFollower;
import com.example.project1.entity.Notification;
import com.example.project1.repository.ArtistFollowerRepository;
import com.example.project1.repository.NotificationRepository;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * ConcreteObserver #1 (the "customer observer"): every customer who follows the artist gets an
 * in-app notification when that artist's content changes. Following = attach, unfollowing = detach.
 */
@Component
public class FollowerNotificationObserver implements MusicContentObserver {

    private final ArtistFollowerRepository followerRepository;
    private final NotificationRepository notificationRepository;

    public FollowerNotificationObserver(ArtistFollowerRepository followerRepository,
                                        NotificationRepository notificationRepository) {
        this.followerRepository = followerRepository;
        this.notificationRepository = notificationRepository;
    }

    @Override
    public void update(MusicContentEvent event) {
        if (event.getArtistId() == null) {
            return;
        }
        List<Notification> notifications = new ArrayList<>();
        for (ArtistFollower follower : followerRepository.findByArtist_ArtistId(event.getArtistId())) {
            notifications.add(new Notification(
                    follower.getUser(), event.getType().name(), event.getMessage(), event.getArtistId()));
        }
        notificationRepository.saveAll(notifications);
    }
}
