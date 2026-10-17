package com.example.project1.repository;

import com.example.project1.entity.Artist;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ArtistRepository extends JpaRepository<Artist, Long> {

    List<Artist> findByArtistNameContainingIgnoreCase(String artistName);

    boolean existsByArtistName(String artistName);
}
