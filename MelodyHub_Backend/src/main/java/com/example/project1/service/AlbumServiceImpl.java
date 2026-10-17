package com.example.project1.service;

import com.example.project1.dto.AlbumRequest;
import com.example.project1.dto.AlbumResponse;
import com.example.project1.entity.Album;
import com.example.project1.entity.Artist;
import com.example.project1.exception.ApiErrors;
import com.example.project1.pattern.observer.MusicContentEvent;
import com.example.project1.pattern.observer.MusicContentPublisher;
import com.example.project1.repository.AlbumRepository;
import com.example.project1.repository.ArtistRepository;
import com.example.project1.repository.SongRepository;
import com.example.project1.storage.FileStorageService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AlbumServiceImpl implements AlbumService {

    private final AlbumRepository albumRepository;
    private final SongRepository songRepository;
    private final ArtistRepository artistRepository;
    private final MusicContentPublisher contentPublisher; // Observer pattern: ConcreteSubject
    private final FileStorageService storage;

    public AlbumServiceImpl(AlbumRepository albumRepository,
                            ArtistRepository artistRepository,
                            MusicContentPublisher contentPublisher,
                            SongRepository songRepository,
                            FileStorageService storage) {
        this.songRepository = songRepository;
        this.contentPublisher = contentPublisher;
        this.albumRepository = albumRepository;
        this.artistRepository = artistRepository;
        this.storage = storage;
    }

    @Override
    public AlbumResponse createAlbum(AlbumRequest request) {
        Artist artist = validate(request, null);
        String coverUrl = storage.resolveJsonImageUrl(null, request.getCoverImageUrl(),
                Boolean.TRUE.equals(request.getRemoveImage()), 255);
        Album album = new Album();
        apply(album, request, artist);
        album.setCoverImageUrl(coverUrl);
        return saveNew(album, artist);
    }

    @Override
    public AlbumResponse createAlbum(AlbumRequest request, MultipartFile image) {
        Artist artist = validate(request, null);
        return storage.replaceImage(null, image, false, FileStorageService.ALBUMS, url -> {
            Album album = new Album();
            apply(album, request, artist);
            album.setCoverImageUrl(url);
            return saveNew(album, artist);
        });
    }

    private AlbumResponse saveNew(Album album, Artist artist) {
        Album savedAlbum = albumRepository.save(album);
        contentPublisher.setContentChange(new MusicContentEvent(
                MusicContentEvent.Type.ALBUM_ADDED, artist.getArtistId(), artist.getArtistName(),
                savedAlbum.getTitle(),
                "New album \"" + savedAlbum.getTitle() + "\" by " + artist.getArtistName()));
        return convertToResponse(savedAlbum);
    }

    @Override
    public List<AlbumResponse> getAllAlbums() {
        return albumRepository.findAll()
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public AlbumResponse getAlbumById(Long id) {
        return convertToResponse(find(id));
    }

    @Override
    public AlbumResponse updateAlbum(Long id, AlbumRequest request) {
        Album album = find(id);
        Artist artist = validate(request, id);
        String oldUrl = album.getCoverImageUrl();
        String newUrl = storage.resolveJsonImageUrl(oldUrl, request.getCoverImageUrl(),
                Boolean.TRUE.equals(request.getRemoveImage()), 255);
        apply(album, request, artist);
        album.setCoverImageUrl(newUrl);
        AlbumResponse saved = saveUpdated(album, artist);
        if (!java.util.Objects.equals(oldUrl, newUrl)) {
            storage.deleteImageQuietly(oldUrl);
        }
        return saved;
    }

    @Override
    public AlbumResponse updateAlbum(Long id, AlbumRequest request, MultipartFile image) {
        Album album = find(id);
        Artist artist = validate(request, id);
        return storage.replaceImage(album.getCoverImageUrl(), image, Boolean.TRUE.equals(request.getRemoveImage()),
                FileStorageService.ALBUMS, url -> {
                    apply(album, request, artist);
                    album.setCoverImageUrl(url);
                    return saveUpdated(album, artist);
                });
    }

    private AlbumResponse saveUpdated(Album album, Artist artist) {
        Album updatedAlbum = albumRepository.save(album);
        contentPublisher.setContentChange(new MusicContentEvent(
                MusicContentEvent.Type.ALBUM_UPDATED, artist.getArtistId(), artist.getArtistName(),
                updatedAlbum.getTitle(),
                "Album \"" + updatedAlbum.getTitle() + "\" by " + artist.getArtistName() + " was updated"));
        return convertToResponse(updatedAlbum);
    }

    @Override
    public String deleteAlbum(Long id) {
        Album album = find(id);
        if (songRepository.existsByAlbum_AlbumId(id)) {
            throw ApiErrors.conflict("This album still has songs. Delete those songs first.");
        }
        String coverUrl = album.getCoverImageUrl();
        albumRepository.delete(album);
        storage.deleteImageQuietly(coverUrl);
        return "Album deleted successfully";
    }

    @Override
    public List<AlbumResponse> getAlbumsByArtist(Long artistId) {
        return albumRepository
                .findByArtist_ArtistId(artistId)
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<AlbumResponse> searchAlbums(String title) {
        return albumRepository
                .findByTitleContainingIgnoreCase(title == null ? "" : title.trim())
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    private Album find(Long id) {
        return albumRepository.findById(ApiErrors.required(id, "Album id is required."))
                .orElseThrow(() -> ApiErrors.notFound("Album not found."));
    }

    /** Checks the request and returns the album's artist. */
    private Artist validate(AlbumRequest request, Long currentId) {
        if (request == null) {
            throw ApiErrors.badRequest("Album details are required.");
        }
        String title = ApiErrors.requiredText(request.getTitle(), "Album title", 255);
        ApiErrors.optionalText(request.getDescription(), "Description", 1000);
        if (request.getReleaseDate() != null) {
            LocalDate latest = LocalDate.now().plusYears(5);
            if (request.getReleaseDate().isBefore(LocalDate.of(1900, 1, 1)) || request.getReleaseDate().isAfter(latest)) {
                throw ApiErrors.badRequest("Please enter a realistic release date.");
            }
        }
        Artist artist = artistRepository.findById(ApiErrors.required(request.getArtistId(), "Please choose an artist."))
                .orElseThrow(() -> ApiErrors.notFound("The selected artist was not found."));
        boolean taken = albumRepository.findByArtist_ArtistId(artist.getArtistId()).stream()
                .anyMatch(a -> a.getTitle() != null && a.getTitle().trim().equalsIgnoreCase(title)
                        && (currentId == null || !a.getAlbumId().equals(currentId)));
        if (taken) {
            throw ApiErrors.conflict("This artist already has an album with this title.");
        }
        return artist;
    }

    private void apply(Album album, AlbumRequest request, Artist artist) {
        album.setTitle(request.getTitle().trim());
        album.setReleaseDate(request.getReleaseDate());
        album.setDescription(ApiErrors.optionalText(request.getDescription(), "Description", 1000));
        album.setArtist(artist);
    }

    private AlbumResponse convertToResponse(Album album) {
        AlbumResponse response = new AlbumResponse(
                album.getAlbumId(),
                album.getTitle(),
                album.getReleaseDate(),
                album.getDescription(),
                album.getCoverImageUrl(),
                album.getArtist().getArtistId(),
                album.getArtist().getArtistName()
        );
        response.setArtistImageUrl(album.getArtist().getImageUrl());
        return response;
    }
}
