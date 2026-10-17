package com.example.project1.service;

import com.example.project1.dto.DigitalMusicResponse;
import com.example.project1.entity.Album;
import com.example.project1.entity.Artist;
import com.example.project1.entity.DigitalMusic;
import com.example.project1.entity.Song;
import com.example.project1.exception.ApiErrors;
import com.example.project1.pattern.observer.MusicContentEvent;
import com.example.project1.pattern.observer.MusicContentPublisher;
import com.example.project1.repository.DigitalMusicRepository;
import com.example.project1.repository.SongRepository;
import com.example.project1.storage.FileStorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Digital music: one MP3 per song.
 * Files are stored with random names under uploads/music and are never served publicly;
 * logged-in users download them through GET /api/digital-music/{id}/download.
 * (No duration matching between the MP3 and the song record - by design.)
 */
@Service
public class DigitalMusicServiceImpl implements DigitalMusicService {

    private static final Logger log = LoggerFactory.getLogger(DigitalMusicServiceImpl.class);
    private static final Set<String> STATUSES = Set.of("ACTIVE", "INACTIVE");
    /** older rows may use AVAILABLE for "downloadable" */
    private static final Set<String> DOWNLOADABLE = Set.of("ACTIVE", "AVAILABLE");

    private final DigitalMusicRepository digitalMusicRepository;
    private final SongRepository songRepository;
    private final MusicContentPublisher contentPublisher; // Observer pattern: ConcreteSubject
    private final FileStorageService storage;

    public DigitalMusicServiceImpl(DigitalMusicRepository digitalMusicRepository,
                                   SongRepository songRepository,
                                   MusicContentPublisher contentPublisher,
                                   FileStorageService storage) {
        this.contentPublisher = contentPublisher;
        this.digitalMusicRepository = digitalMusicRepository;
        this.songRepository = songRepository;
        this.storage = storage;
    }

    // ------------------------------------------------------------------ upload

    @Override
    public DigitalMusicResponse uploadMusic(Long songId, MultipartFile file, String status) {
        Song song = songRepository.findById(ApiErrors.required(songId, "Please select a song."))
                .orElseThrow(() -> ApiErrors.notFound("The selected song was not found."));
        String cleanStatus = normaliseStatus(status);
        if (digitalMusicRepository.existsBySong_SongId(songId)) {
            throw ApiErrors.conflict("This song already has an MP3. Delete the existing file before uploading a new one.");
        }

        // validates type, size and content, then stores under a random name
        FileStorageService.StoredAudio stored = storage.storeMp3(file);

        DigitalMusic dm = new DigitalMusic();
        dm.setSong(song);
        dm.setFileName(cleanOriginalName(file.getOriginalFilename()));
        dm.setFilePath(stored.storedPath());
        dm.setFileUrl("/uploads/music/" + stored.storedName()); // logical location; not publicly served
        dm.setFileFormat("MP3");
        dm.setFileSize(stored.size());
        dm.setUploadedAt(LocalDateTime.now());
        dm.setStatus(cleanStatus);

        DigitalMusic saved;
        try {
            saved = digitalMusicRepository.save(dm);
        } catch (DataIntegrityViolationException e) {
            storage.deleteStoredFileQuietly(stored.storedPath());
            throw ApiErrors.conflict("This song already has an MP3 or the upload conflicts with existing data.");
        } catch (RuntimeException e) {
            storage.deleteStoredFileQuietly(stored.storedPath());
            throw e;
        }

        if ("ACTIVE".equals(saved.getStatus())) {
            publishRelease(song);
        }
        return convertToResponse(saved);
    }

    private void publishRelease(Song song) {
        Album album = song.getAlbum();
        Artist artist = album == null ? null : album.getArtist();
        if (artist == null) {
            return;
        }
        try {
            contentPublisher.setContentChange(new MusicContentEvent(
                    MusicContentEvent.Type.MUSIC_PUBLISHED, artist.getArtistId(), artist.getArtistName(), song.getTitle(),
                    "\"" + song.getTitle() + "\" by " + artist.getArtistName() + " is now available as digital music"));
        } catch (RuntimeException e) {
            log.warn("Could not notify followers about a new track: {}", e.getMessage());
        }
    }

    // ------------------------------------------------------------------ read

    @Override
    public List<DigitalMusicResponse> getAllMusic(boolean includeInactive) {
        return digitalMusicRepository.findAll().stream()
                .filter(m -> includeInactive || isDownloadable(m))
                .sorted(Comparator.comparing(DigitalMusic::getUploadedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public DigitalMusicResponse getMusicById(Long id, boolean includeInactive) {
        DigitalMusic music = find(id);
        if (!includeInactive && !isDownloadable(music)) {
            throw ApiErrors.notFound("Digital music not found.");
        }
        return convertToResponse(music);
    }

    @Override
    public DigitalMusicResponse getMusicBySong(Long songId, boolean includeInactive) {
        DigitalMusic music = digitalMusicRepository.findBySong_SongId(songId)
                .filter(m -> includeInactive || isDownloadable(m))
                .orElseThrow(() -> ApiErrors.notFound("This song has no digital music yet."));
        return convertToResponse(music);
    }

    // ------------------------------------------------------------------ manage

    @Override
    public DigitalMusicResponse updateStatus(Long id, String status) {
        DigitalMusic music = find(id);
        String cleanStatus = normaliseStatus(status);
        boolean becameActive = "ACTIVE".equals(cleanStatus) && !isDownloadable(music);
        music.setStatus(cleanStatus);
        DigitalMusic saved = digitalMusicRepository.save(music);
        if (becameActive && saved.getSong() != null) {
            publishRelease(saved.getSong());
        }
        return convertToResponse(saved);
    }

    @Override
    public String deleteMusic(Long id) {
        DigitalMusic music = find(id);
        String path = music.getFilePath();
        digitalMusicRepository.delete(music);
        storage.deleteStoredFileQuietly(path); // after the row is gone, so no record points at a missing file
        return "Digital music deleted successfully";
    }

    @Override
    public MusicDownload prepareDownload(Long id, boolean manager) {
        DigitalMusic music = find(id);
        if (!manager && !isDownloadable(music)) {
            throw ApiErrors.notFound("This track is not available for download.");
        }
        Path path = storage.resolveStoredFile(music.getFilePath());
        if (path == null) {
            log.warn("MP3 for digital music #{} is missing on disk ({})", id, music.getFilePath());
            throw ApiErrors.notFound("The MP3 file for this track is no longer available. Please contact support.");
        }
        long size;
        try {
            size = Files.size(path);
        } catch (java.io.IOException e) {
            throw ApiErrors.notFound("The MP3 file for this track could not be read.");
        }
        return new MusicDownload(path, downloadName(music), size);
    }

    // ------------------------------------------------------------------ helpers

    private DigitalMusic find(Long id) {
        return digitalMusicRepository.findById(ApiErrors.required(id, "Digital music id is required."))
                .orElseThrow(() -> ApiErrors.notFound("Digital music not found."));
    }

    private static boolean isDownloadable(DigitalMusic music) {
        return music.getStatus() == null || DOWNLOADABLE.contains(music.getStatus().trim().toUpperCase(Locale.ROOT));
    }

    private static String normaliseStatus(String status) {
        if (status == null || status.isBlank()) {
            return "ACTIVE";
        }
        String clean = status.trim().toUpperCase(Locale.ROOT);
        if (!STATUSES.contains(clean)) {
            throw ApiErrors.badRequest("Status must be ACTIVE or INACTIVE.");
        }
        return clean;
    }

    /** Only the last path segment of the browser's file name, without control characters. */
    private static String cleanOriginalName(String original) {
        if (original == null || original.isBlank()) {
            return "track.mp3";
        }
        String name = original.replace('\\', '/');
        name = name.substring(name.lastIndexOf('/') + 1).replaceAll("[\\p{Cntrl}\"]", "").trim();
        if (name.length() > 200) {
            name = name.substring(name.length() - 200);
        }
        return name.isEmpty() ? "track.mp3" : name;
    }

    /** "Artist - Song.mp3", safe for every operating system. */
    private static String downloadName(DigitalMusic music) {
        Song song = music.getSong();
        String title = song != null ? song.getTitle() : null;
        Artist artist = song != null && song.getAlbum() != null ? song.getAlbum().getArtist() : null;
        String base = title == null ? null : (artist != null ? artist.getArtistName() + " - " + title : title);
        if (base == null || base.isBlank()) {
            base = music.getFileName() == null ? "track" : music.getFileName().replaceAll("(?i)\\.mp3$", "");
        }
        base = base.replaceAll("[\\\\/:*?\"<>|\\p{Cntrl}]", "").replaceAll("\\s+", " ").trim();
        if (base.length() > 120) {
            base = base.substring(0, 120).trim();
        }
        return (base.isEmpty() ? "track" : base) + ".mp3";
    }

    private DigitalMusicResponse convertToResponse(DigitalMusic music) {
        Song song = music.getSong();
        Album album = song != null ? song.getAlbum() : null;
        Artist artist = album != null ? album.getArtist() : null;
        DigitalMusicResponse r = new DigitalMusicResponse();
        r.setDigitalMusicId(music.getDigitalMusicId());
        r.setSongId(song != null ? song.getSongId() : null);
        r.setSongTitle(song != null ? song.getTitle() : null);
        r.setDuration(song != null ? song.getDuration() : null);
        r.setAlbumId(album != null ? album.getAlbumId() : null);
        r.setAlbumTitle(album != null ? album.getTitle() : null);
        r.setCoverImageUrl(album != null ? album.getCoverImageUrl() : null);
        r.setArtistId(artist != null ? artist.getArtistId() : null);
        r.setArtistName(artist != null ? artist.getArtistName() : null);
        r.setArtistImageUrl(artist != null ? artist.getImageUrl() : null);
        r.setFileName(music.getFileName());
        r.setFileUrl("/api/digital-music/" + music.getDigitalMusicId() + "/download");
        r.setFileFormat(music.getFileFormat());
        r.setFileSize(music.getFileSize());
        r.setUploadedAt(music.getUploadedAt());
        r.setStatus(music.getStatus());
        return r;
    }
}
