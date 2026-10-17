package com.example.project1.pattern.observer;

import com.example.project1.entity.Notification;
import com.example.project1.entity.User;
import com.example.project1.enums.Role;
import com.example.project1.repository.NotificationRepository;
import com.example.project1.repository.UserRepository;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * ConcreteObserver #2 (the "notification observer" for the shop team): admins and marketing
 * managers are told when NEW content is added or published, so promotions can be planned.
 * Routine edits are ignored by this observer (it chooses what it reacts to in update()).
 */
@Component
public class StaffNotificationObserver implements MusicContentObserver {

    private static final Set<MusicContentEvent.Type> INTERESTING = EnumSet.of(
            MusicContentEvent.Type.ALBUM_ADDED,
            MusicContentEvent.Type.SONG_ADDED,
            MusicContentEvent.Type.MUSIC_PUBLISHED);

    private final UserRepository userRepository;
    private final NotificationRepository notificationRepository;

    public StaffNotificationObserver(UserRepository userRepository,
                                     NotificationRepository notificationRepository) {
        this.userRepository = userRepository;
        this.notificationRepository = notificationRepository;
    }

    @Override
    public void update(MusicContentEvent event) {
        if (!INTERESTING.contains(event.getType())) {
            return;
        }
        List<Notification> notifications = new ArrayList<>();
        for (User staff : userRepository.findByRoleIn(EnumSet.of(Role.ADMIN, Role.MARKETING_MANAGER))) {
            notifications.add(new Notification(
                    staff, event.getType().name(), "[Catalog] " + event.getMessage(), event.getArtistId()));
        }
        notificationRepository.saveAll(notifications);
    }
}
