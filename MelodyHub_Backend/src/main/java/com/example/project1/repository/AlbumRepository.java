package com.example.project1.repository;

import com.example.project1.entity.Album;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AlbumRepository extends JpaRepository<Album, Long> {

    List<Album> findByArtist_ArtistId(Long artistId);

    boolean existsByArtist_ArtistId(Long artistId);

    List<Album> findByTitleContainingIgnoreCase(String title);

    boolean existsByTitleAndArtist_ArtistId(
            String title,
            Long artistId
    );
}
