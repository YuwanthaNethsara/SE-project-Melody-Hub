package com.example.project1.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/** A customer who follows an artist - the customer side of the Observer relationship. */
@Entity
@Table(name = "artist_followers",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "artist_id"}))
public class ArtistFollower {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long followerId;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "artist_id", nullable = false)
    private Artist artist;

    @Column(nullable = false)
    private LocalDateTime followedAt;

    public ArtistFollower() {
    }

    public ArtistFollower(User user, Artist artist) {
        this.user = user;
        this.artist = artist;
        this.followedAt = LocalDateTime.now();
    }

    public Long getFollowerId() { return followerId; }
    public User getUser() { return user; }
    public Artist getArtist() { return artist; }
    public LocalDateTime getFollowedAt() { return followedAt; }
}
