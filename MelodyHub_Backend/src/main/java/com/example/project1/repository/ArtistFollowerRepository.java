package com.example.project1.repository;

import com.example.project1.entity.ArtistFollower;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ArtistFollowerRepository extends JpaRepository<ArtistFollower, Long> {

    List<ArtistFollower> findByArtist_ArtistId(Long artistId);

    List<ArtistFollower> findByUser_Id(Long userId);

    Optional<ArtistFollower> findByUser_IdAndArtist_ArtistId(Long userId, Long artistId);
}
