package com.example.project1.service;

import com.example.project1.dto.ArtistResponse;
import com.example.project1.dto.NotificationResponse;
import com.example.project1.entity.Artist;
import com.example.project1.entity.ArtistFollower;
import com.example.project1.entity.Notification;
import com.example.project1.entity.User;
import com.example.project1.repository.ArtistFollowerRepository;
import com.example.project1.repository.ArtistRepository;
import com.example.project1.repository.NotificationRepository;
import com.example.project1.repository.UserRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class NotificationServiceImpl implements NotificationService {

    private final UserRepository userRepository;
    private final ArtistRepository artistRepository;
    private final ArtistFollowerRepository followerRepository;
    private final NotificationRepository notificationRepository;

    public NotificationServiceImpl(UserRepository userRepository,
                                   ArtistRepository artistRepository,
                                   ArtistFollowerRepository followerRepository,
                                   NotificationRepository notificationRepository) {
        this.userRepository = userRepository;
        this.artistRepository = artistRepository;
        this.followerRepository = followerRepository;
        this.notificationRepository = notificationRepository;
    }

    @Override
    @Transactional
    public String follow(String email, Long artistId) {
        User user = currentUser(email);
        Artist artist = artistRepository.findById(artistId)
                .orElseThrow(() -> new RuntimeException("Artist not found"));

        if (followerRepository.findByUser_IdAndArtist_ArtistId(user.getId(), artistId).isPresent()) {
            return "You already follow " + artist.getArtistName();
        }
        followerRepository.save(new ArtistFollower(user, artist));
        return "You now follow " + artist.getArtistName();
    }

    @Override
    @Transactional
    public String unfollow(String email, Long artistId) {
        User user = currentUser(email);
        followerRepository.findByUser_IdAndArtist_ArtistId(user.getId(), artistId)
                .ifPresent(followerRepository::delete);
        return "You no longer follow this artist";
    }

    @Override
    @Transactional(readOnly = true)
    public List<ArtistResponse> getFollowedArtists(String email) {
        User user = currentUser(email);
        return followerRepository.findByUser_Id(user.getId()).stream()
                .map(ArtistFollower::getArtist)
                .map(a -> new ArtistResponse(a.getArtistId(), a.getArtistName(), a.getBio(),
                        a.getCountry(), a.getGenre(), a.getImageUrl()))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationResponse> getMyNotifications(String email) {
        User user = currentUser(email);
        return notificationRepository.findByUser_IdOrderByCreatedAtDesc(user.getId()).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public long countUnread(String email) {
        return notificationRepository.countByUser_IdAndSeenFalse(currentUser(email).getId());
    }

    @Override
    @Transactional
    public NotificationResponse markAsRead(String email, Long notificationId) {
        User user = currentUser(email);
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));
        // a user can only touch their own notifications
        if (!notification.getUser().getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "You do not have permission to perform this action.");
        }
        notification.setSeen(true);
        return toResponse(notificationRepository.save(notification));
    }

    @Override
    @Transactional
    public int markAllAsRead(String email) {
        List<Notification> unread = notificationRepository.findByUser_IdAndSeenFalse(currentUser(email).getId());
        unread.forEach(n -> n.setSeen(true));
        notificationRepository.saveAll(unread);
        return unread.size();
    }

    private User currentUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    private NotificationResponse toResponse(Notification n) {
        return new NotificationResponse(n.getNotificationId(), n.getType(), n.getMessage(),
                n.getArtistId(), n.isSeen(), n.getCreatedAt());
    }
}
