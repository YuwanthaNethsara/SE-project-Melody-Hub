package com.example.project1.controller;

import com.example.project1.dto.DigitalMusicResponse;
import com.example.project1.service.DigitalMusicService;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

/**
 * Any logged-in user: list ACTIVE tracks and download them.
 * ADMIN / ARTIST: see every track, upload, change status, delete (see SecurityConfig).
 */
@RestController
@RequestMapping("/api/digital-music")
public class DigitalMusicController {

    private final DigitalMusicService digitalMusicService;

    public DigitalMusicController(DigitalMusicService digitalMusicService) {
        this.digitalMusicService = digitalMusicService;
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<DigitalMusicResponse> uploadMusic(
            @RequestParam("songId") Long songId,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "status", required = false) String status) {
        return new ResponseEntity<>(digitalMusicService.uploadMusic(songId, file, status), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<DigitalMusicResponse>> getAllMusic(Authentication authentication) {
        return ResponseEntity.ok(digitalMusicService.getAllMusic(isManager(authentication)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<DigitalMusicResponse> getMusicById(@PathVariable Long id, Authentication authentication) {
        return ResponseEntity.ok(digitalMusicService.getMusicById(id, isManager(authentication)));
    }

    @GetMapping("/song/{songId}")
    public ResponseEntity<DigitalMusicResponse> getMusicBySong(@PathVariable Long songId,
                                                               Authentication authentication) {
        return ResponseEntity.ok(digitalMusicService.getMusicBySong(songId, isManager(authentication)));
    }

    /** Streams the MP3 as an attachment. Requires a valid login (Authorization: Bearer ...). */
    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> downloadMusic(@PathVariable Long id, Authentication authentication) {
        DigitalMusicService.MusicDownload download = digitalMusicService.prepareDownload(id, isManager(authentication));
        String name = download.downloadName();
        String asciiName = name.replaceAll("[^\\x20-\\x7E]", "_").replace("\"", "");
        String encoded = URLEncoder.encode(name, StandardCharsets.UTF_8).replace("+", "%20");
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("audio/mpeg"))
                .contentLength(download.size())
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + asciiName + "\"; filename*=UTF-8''" + encoded)
                .header(HttpHeaders.CACHE_CONTROL, "no-store")
                .body(new FileSystemResource(download.path()));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<DigitalMusicResponse> updateStatus(@PathVariable Long id, @RequestParam String status) {
        return ResponseEntity.ok(digitalMusicService.updateStatus(id, status));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteMusic(@PathVariable Long id) {
        return ResponseEntity.ok(Map.of("message", digitalMusicService.deleteMusic(id)));
    }

    private static boolean isManager(Authentication authentication) {
        return authentication != null && authentication.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()) || "ROLE_ARTIST".equals(a.getAuthority()));
    }
}
