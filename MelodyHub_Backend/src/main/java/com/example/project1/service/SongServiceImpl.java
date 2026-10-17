package com.example.project1.service;

import com.example.project1.exception.ApiErrors;

import com.example.project1.dto.SongRequest;
import com.example.project1.dto.SongResponse;
import com.example.project1.entity.Album;
import com.example.project1.entity.Song;
import com.example.project1.pattern.observer.MusicContentEvent;
import com.example.project1.pattern.observer.MusicContentPublisher;
import com.example.project1.repository.AlbumRepository;
import com.example.project1.repository.SongRepository;

import com.example.project1.repository.DigitalMusicRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SongServiceImpl implements SongService {

    private final SongRepository songRepository;
    private final DigitalMusicRepository digitalMusicRepository;
    private final AlbumRepository albumRepository;
    private final MusicContentPublisher contentPublisher; // Observer pattern: ConcreteSubject

    public SongServiceImpl(
            SongRepository songRepository,
            AlbumRepository albumRepository,
            MusicContentPublisher contentPublisher,
            DigitalMusicRepository digitalMusicRepository
    ) {
        this.digitalMusicRepository = digitalMusicRepository;

        this.contentPublisher = contentPublisher;
        this.songRepository = songRepository;
        this.albumRepository = albumRepository;
    }

    @Override
    public SongResponse createSong(SongRequest request) {

        Album album = validate(request, null);

        Song song = new Song();

        song.setTitle(request.getTitle().trim());
        song.setDuration(request.getDuration() == null || request.getDuration().isBlank() ? null : request.getDuration().trim());
        song.setTrackNumber(request.getTrackNumber());
        song.setAlbum(album);

        Song savedSong = songRepository.save(song);

        // OBSERVER: tell every registered observer that a new song was added
        contentPublisher.setContentChange(new MusicContentEvent(
                MusicContentEvent.Type.SONG_ADDED, album.getArtist().getArtistId(),
                album.getArtist().getArtistName(), savedSong.getTitle(),
                "New song \"" + savedSong.getTitle() + "\" added to \"" + album.getTitle()
                        + "\" by " + album.getArtist().getArtistName()));

        return convertToResponse(savedSong);
    }

    @Override
    public List<SongResponse> getAllSongs() {

        return songRepository.findAll()
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public SongResponse getSongById(Long id) {

        return convertToResponse(find(id));
    }

    @Override
    public SongResponse updateSong(
            Long id,
            SongRequest request) {

        Song song = find(id);

        Album album = validate(request, id);

        song.setTitle(request.getTitle().trim());
        song.setDuration(request.getDuration() == null || request.getDuration().isBlank() ? null : request.getDuration().trim());
        song.setTrackNumber(request.getTrackNumber());
        song.setAlbum(album);

        Song updatedSong = songRepository.save(song);

        contentPublisher.setContentChange(new MusicContentEvent(
                MusicContentEvent.Type.SONG_UPDATED, album.getArtist().getArtistId(),
                album.getArtist().getArtistName(), updatedSong.getTitle(),
                "Song \"" + updatedSong.getTitle() + "\" by " + album.getArtist().getArtistName()
                        + " was updated"));

        return convertToResponse(updatedSong);
    }

    @Override
    public String deleteSong(Long id) {

        if (!songRepository.existsById(ApiErrors.required(id, "Song id is required."))) {
            throw ApiErrors.notFound("Song not found.");
        }

        if (digitalMusicRepository.existsBySong_SongId(id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "This song has an uploaded audio file. Delete the audio file first.");
        }

        songRepository.deleteById(id);

        return "Song deleted successfully";
    }

    @Override
    public List<SongResponse> getSongsByAlbum(
            Long albumId) {

        return songRepository
                .findByAlbum_AlbumId(albumId)
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<SongResponse> searchSongs(String title) {

        return songRepository
                .findByTitleContainingIgnoreCase(title)
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    private Song find(Long id) {
        return songRepository.findById(ApiErrors.required(id, "Song id is required."))
                .orElseThrow(() -> ApiErrors.notFound("Song not found."));
    }

    /** Checks the request and returns the song's album. */
    private Album validate(SongRequest request, Long currentId) {
        if (request == null) {
            throw ApiErrors.badRequest("Song details are required.");
        }
        String title = ApiErrors.requiredText(request.getTitle(), "Song title", 255);
        if (request.getTrackNumber() != null && (request.getTrackNumber() < 1 || request.getTrackNumber() > 999)) {
            throw ApiErrors.badRequest("Track number must be between 1 and 999.");
        }
        String duration = request.getDuration() == null ? "" : request.getDuration().trim();
        if (!duration.isEmpty() && !duration.matches("^(\\d{1,3}:[0-5]\\d|\\d{1,2}:[0-5]\\d:[0-5]\\d)$")) {
            throw ApiErrors.badRequest("Duration must look like 3:45 (minutes:seconds).");
        }
        Album album = albumRepository.findById(ApiErrors.required(request.getAlbumId(), "Please choose an album."))
                .orElseThrow(() -> ApiErrors.notFound("The selected album was not found."));
        boolean taken = songRepository.findByAlbum_AlbumId(album.getAlbumId()).stream()
                .anyMatch(x -> x.getTitle() != null && x.getTitle().trim().equalsIgnoreCase(title)
                        && (currentId == null || !x.getSongId().equals(currentId)));
        if (taken) {
            throw ApiErrors.conflict("This album already has a song with this title.");
        }
        return album;
    }

    private SongResponse convertToResponse(Song song) {

        SongResponse response = new SongResponse(
                song.getSongId(),
                song.getTitle(),
                song.getDuration(),
                song.getTrackNumber(),
                song.getAlbum().getAlbumId(),
                song.getAlbum().getTitle(),
                song.getAlbum().getArtist().getArtistId(),
                song.getAlbum().getArtist().getArtistName()
        );
        response.setArtistImageUrl(song.getAlbum().getArtist().getImageUrl());
        return response;
    }
}
