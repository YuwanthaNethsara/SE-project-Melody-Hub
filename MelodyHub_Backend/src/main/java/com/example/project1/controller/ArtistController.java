package com.example.project1.controller;

import com.example.project1.dto.ArtistRequest;
import com.example.project1.dto.ArtistResponse;
import com.example.project1.service.ArtistService;
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
@RequestMapping("/api/artists")
public class ArtistController {

    private final ArtistService artistService;

    public ArtistController(ArtistService artistService) {
        this.artistService = artistService;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ArtistResponse> createWithImage(
            @RequestPart("data") ArtistRequest request,
            @RequestPart(value = "image", required = false) MultipartFile image) {
        return new ResponseEntity<>(artistService.createArtist(request, image), HttpStatus.CREATED);
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ArtistResponse> createArtist(@RequestBody ArtistRequest request) {
        return new ResponseEntity<>(artistService.createArtist(request), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<ArtistResponse>> getAllArtists() {
        return ResponseEntity.ok(artistService.getAllArtists());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ArtistResponse> getArtistById(@PathVariable Long id) {
        return ResponseEntity.ok(artistService.getArtistById(id));
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ArtistResponse> updateWithImage(
            @PathVariable Long id,
            @RequestPart("data") ArtistRequest request,
            @RequestPart(value = "image", required = false) MultipartFile image) {
        return ResponseEntity.ok(artistService.updateArtist(id, request, image));
    }

    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ArtistResponse> updateArtist(
            @PathVariable Long id, @RequestBody ArtistRequest request) {
        return ResponseEntity.ok(artistService.updateArtist(id, request));
    }

    /**
     * Uploads or replaces only the artist photo: multipart/form-data with one part called "image"
     * (JPG, JPEG, PNG or WEBP, max 2 MB by default). POST and PUT both work.
     */
    @PostMapping(value = "/{id}/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ArtistResponse> uploadImage(
            @PathVariable Long id,
            @RequestPart(value = "image", required = false) MultipartFile image) {
        return ResponseEntity.ok(artistService.updateArtistImage(id, image));
    }

    @PutMapping(value = "/{id}/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ArtistResponse> replaceImage(
            @PathVariable Long id,
            @RequestPart(value = "image", required = false) MultipartFile image) {
        return ResponseEntity.ok(artistService.updateArtistImage(id, image));
    }

    /** Removes the artist photo (the profile falls back to the default picture). */
    @DeleteMapping("/{id}/image")
    public ResponseEntity<ArtistResponse> removeImage(@PathVariable Long id) {
        return ResponseEntity.ok(artistService.removeArtistImage(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteArtist(@PathVariable Long id) {
        return ResponseEntity.ok(artistService.deleteArtist(id));
    }

    @GetMapping("/search")
    public ResponseEntity<List<ArtistResponse>> searchArtists(@RequestParam String name) {
        return ResponseEntity.ok(artistService.searchArtists(name));
    }
}
