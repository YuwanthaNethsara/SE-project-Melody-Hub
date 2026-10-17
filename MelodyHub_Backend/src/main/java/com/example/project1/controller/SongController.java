package com.example.project1.controller;

import com.example.project1.dto.SongRequest;
import com.example.project1.dto.SongResponse;
import com.example.project1.service.SongService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/songs")
public class SongController {

    private final SongService songService;

    public SongController(SongService songService) {
        this.songService = songService;
    }

    @PostMapping
    public ResponseEntity<SongResponse> createSong(
            @RequestBody SongRequest request) {

        return ResponseEntity.ok(
                songService.createSong(request)
        );
    }

    @GetMapping
    public ResponseEntity<List<SongResponse>> getAllSongs() {

        return ResponseEntity.ok(
                songService.getAllSongs()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<SongResponse> getSongById(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                songService.getSongById(id)
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<SongResponse> updateSong(
            @PathVariable Long id,
            @RequestBody SongRequest request) {

        return ResponseEntity.ok(
                songService.updateSong(id, request)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteSong(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                songService.deleteSong(id)
        );
    }

    @GetMapping("/album/{albumId}")
    public ResponseEntity<List<SongResponse>>
    getSongsByAlbum(
            @PathVariable Long albumId) {

        return ResponseEntity.ok(
                songService.getSongsByAlbum(albumId)
        );
    }

    @GetMapping("/search")
    public ResponseEntity<List<SongResponse>>
    searchSongs(
            @RequestParam String title) {

        return ResponseEntity.ok(
                songService.searchSongs(title)
        );
    }
}
