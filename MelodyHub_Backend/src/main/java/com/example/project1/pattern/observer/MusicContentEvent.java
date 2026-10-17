package com.example.project1.pattern.observer;

import java.time.LocalDateTime;

/** The "state" the ConcreteSubject hands to its observers when music content changes. */
public class MusicContentEvent {

    public enum Type { ARTIST_UPDATED, ALBUM_ADDED, ALBUM_UPDATED, SONG_ADDED, SONG_UPDATED, MUSIC_PUBLISHED }

    private final Type type;
    private final Long artistId;
    private final String artistName;
    private final String title;
    private final String message;
    private final LocalDateTime occurredAt = LocalDateTime.now();

    public MusicContentEvent(Type type, Long artistId, String artistName, String title, String message) {
        this.type = type;
        this.artistId = artistId;
        this.artistName = artistName;
        this.title = title;
        this.message = message;
    }

    public Type getType() { return type; }
    public Long getArtistId() { return artistId; }
    public String getArtistName() { return artistName; }
    public String getTitle() { return title; }
    public String getMessage() { return message; }
    public LocalDateTime getOccurredAt() { return occurredAt; }
}
