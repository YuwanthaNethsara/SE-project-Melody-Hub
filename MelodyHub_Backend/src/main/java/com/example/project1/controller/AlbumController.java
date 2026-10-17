package com.example.project1.controller;

import com.example.project1.dto.AlbumRequest;
import com.example.project1.dto.AlbumResponse;
import com.example.project1.service.AlbumService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * GET endpoints are public. Changes: ADMIN and ARTIST.
 * Create / update accept multipart (part "data" + optional file part "image") or plain JSON.
 */
@RestController
@RequestMapping("/api/albums")
public class AlbumController {

    private final AlbumService albumService;

    public AlbumController(AlbumService albumService) {
        this.albumService = albumService;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<AlbumResponse> createWithImage(
            @RequestPart("data") AlbumRequest request,
            @RequestPart(value = "image", required = false) MultipartFile image) {
        return new ResponseEntity<>(albumService.createAlbum(request, image), HttpStatus.CREATED);
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AlbumResponse> createAlbum(@RequestBody AlbumRequest request) {
        return new ResponseEntity<>(albumService.createAlbum(request), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<AlbumResponse>> getAllAlbums() {
        return ResponseEntity.ok(albumService.getAllAlbums());
    }

    @GetMapping("/{id}")
    public ResponseEntity<AlbumResponse> getAlbumById(@PathVariable Long id) {
        return ResponseEntity.ok(albumService.getAlbumById(id));
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<AlbumResponse> updateWithImage(
            @PathVariable Long id,
            @RequestPart("data") AlbumRequest request,
            @RequestPart(value = "image", required = false) MultipartFile image) {
        return ResponseEntity.ok(albumService.updateAlbum(id, request, image));
    }

    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AlbumResponse> updateAlbum(
            @PathVariable Long id, @RequestBody AlbumRequest request) {
        return ResponseEntity.ok(albumService.updateAlbum(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteAlbum(@PathVariable Long id) {
        return ResponseEntity.ok(albumService.deleteAlbum(id));
    }

    @GetMapping("/artist/{artistId}")
    public ResponseEntity<List<AlbumResponse>> getAlbumsByArtist(@PathVariable Long artistId) {
        return ResponseEntity.ok(albumService.getAlbumsByArtist(artistId));
    }

    @GetMapping("/search")
    public ResponseEntity<List<AlbumResponse>> searchAlbums(@RequestParam String title) {
        return ResponseEntity.ok(albumService.searchAlbums(title));
    }
}
