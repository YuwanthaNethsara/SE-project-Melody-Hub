package com.example.project1.repository;

import com.example.project1.entity.Song;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SongRepository extends JpaRepository<Song, Long> {

    List<Song> findByAlbum_AlbumId(Long albumId);

    boolean existsByAlbum_AlbumId(Long albumId);

    List<Song> findByTitleContainingIgnoreCase(String title);

    boolean existsByTitleAndAlbum_AlbumId(
            String title,
            Long albumId
    );
}
