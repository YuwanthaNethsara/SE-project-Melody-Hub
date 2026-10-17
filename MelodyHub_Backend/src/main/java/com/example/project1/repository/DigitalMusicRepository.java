package com.example.project1.repository;

import com.example.project1.entity.DigitalMusic;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DigitalMusicRepository
        extends JpaRepository<DigitalMusic, Long> {

    Optional<DigitalMusic> findBySong_SongId(Long songId);

    boolean existsBySong_SongId(Long songId);
}