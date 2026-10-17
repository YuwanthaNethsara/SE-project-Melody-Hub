package com.example.project1.service;

import com.example.project1.dto.ArtistRequest;
import com.example.project1.dto.ArtistResponse;
import com.example.project1.entity.Artist;
import com.example.project1.exception.ApiErrors;
import com.example.project1.pattern.observer.MusicContentEvent;
import com.example.project1.pattern.observer.MusicContentPublisher;
import com.example.project1.repository.AlbumRepository;
import com.example.project1.repository.ArtistFollowerRepository;
import com.example.project1.repository.ArtistRepository;
import com.example.project1.storage.FileStorageService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ArtistServiceImpl implements ArtistService {

    private final ArtistRepository artistRepository;
    private final AlbumRepository albumRepository;
    private final ArtistFollowerRepository artistFollowerRepository;
    private final MusicContentPublisher contentPublisher; // Observer pattern: ConcreteSubject
    private final FileStorageService storage;

    public ArtistServiceImpl(ArtistRepository artistRepository,
                             MusicContentPublisher contentPublisher,
                             AlbumRepository albumRepository,
                             ArtistFollowerRepository artistFollowerRepository,
                             FileStorageService storage) {
        this.artistFollowerRepository = artistFollowerRepository;
        this.albumRepository = albumRepository;
        this.artistRepository = artistRepository;
        this.contentPublisher = contentPublisher;
        this.storage = storage;
    }

    @Override
    public ArtistResponse createArtist(ArtistRequest request) {
        validate(request, null);
        String imageUrl = storage.resolveJsonImageUrl(null, request.getImageUrl(),
                Boolean.TRUE.equals(request.getRemoveImage()), 255);
        Artist artist = new Artist();
        apply(artist, request);
        artist.setImageUrl(imageUrl);
        return convertToResponse(artistRepository.save(artist));
    }

    @Override
    public ArtistResponse createArtist(ArtistRequest request, MultipartFile image) {
        validate(request, null);
        return storage.replaceImage(null, image, false, FileStorageService.ARTISTS, url -> {
            Artist artist = new Artist();
            apply(artist, request);
            artist.setImageUrl(url);
            return convertToResponse(artistRepository.save(artist));
        });
    }

    @Override
    public List<ArtistResponse> getAllArtists() {
        return artistRepository.findAll()
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public ArtistResponse getArtistById(Long id) {
        return convertToResponse(find(id));
    }

    @Override
    public ArtistResponse updateArtist(Long id, ArtistRequest request) {
        Artist artist = find(id);
        validate(request, id);
        String oldUrl = artist.getImageUrl();
        // no imageUrl in the JSON = keep the current photo (only removeImage=true or "" removes it)
        String newUrl = storage.resolveJsonImageUrl(oldUrl, request.getImageUrl(),
                Boolean.TRUE.equals(request.getRemoveImage()), 255);
        apply(artist, request);
        artist.setImageUrl(newUrl);
        ArtistResponse saved = saveAndNotify(artist);
        if (!java.util.Objects.equals(oldUrl, newUrl)) {
            storage.deleteImageQuietly(oldUrl);
        }
        return saved;
    }

    @Override
    public ArtistResponse updateArtist(Long id, ArtistRequest request, MultipartFile image) {
        Artist artist = find(id);
        validate(request, id);
        return storage.replaceImage(artist.getImageUrl(), image, Boolean.TRUE.equals(request.getRemoveImage()),
                FileStorageService.ARTISTS, url -> {
                    apply(artist, request);
                    artist.setImageUrl(url);
                    return saveAndNotify(artist);
                });
    }

    @Override
    public ArtistResponse updateArtistImage(Long id, MultipartFile image) {
        Artist artist = find(id);
        if (image == null || image.isEmpty()) {
            throw ApiErrors.badRequest("Please choose a photo to upload.");
        }
        // the new file is stored first; the old one is deleted only after the record is saved
        return storage.replaceImage(artist.getImageUrl(), image, false, FileStorageService.ARTISTS, url -> {
            artist.setImageUrl(url);
            return saveAndNotify(artist);
        });
    }

    @Override
    public ArtistResponse removeArtistImage(Long id) {
        Artist artist = find(id);
        if (artist.getImageUrl() == null) {
            return convertToResponse(artist);
        }
        return storage.replaceImage(artist.getImageUrl(), null, true, FileStorageService.ARTISTS, url -> {
            artist.setImageUrl(url);
            return convertToResponse(artistRepository.save(artist));
        });
    }

    private ArtistResponse saveAndNotify(Artist artist) {
        Artist updatedArtist = artistRepository.save(artist);
        contentPublisher.setContentChange(new MusicContentEvent(
                MusicContentEvent.Type.ARTIST_UPDATED, updatedArtist.getArtistId(),
                updatedArtist.getArtistName(), updatedArtist.getArtistName(),
                updatedArtist.getArtistName() + " updated their artist profile"));
        return convertToResponse(updatedArtist);
    }

    @Override
    @Transactional
    public String deleteArtist(Long id) {
        Artist artist = find(id);
        if (albumRepository.existsByArtist_ArtistId(id)) {
            throw ApiErrors.conflict("This artist still has albums. Delete or move those albums first.");
        }
        String imageUrl = artist.getImageUrl();
        artistFollowerRepository.deleteAll(artistFollowerRepository.findByArtist_ArtistId(id));
        artistRepository.delete(artist);
        storage.deleteImageQuietly(imageUrl);
        return "Artist deleted successfully";
    }

    @Override
    public List<ArtistResponse> searchArtists(String name) {
        return artistRepository
                .findByArtistNameContainingIgnoreCase(name == null ? "" : name.trim())
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    private Artist find(Long id) {
        return artistRepository.findById(ApiErrors.required(id, "Artist id is required."))
                .orElseThrow(() -> ApiErrors.notFound("Artist not found."));
    }

    private void validate(ArtistRequest request, Long currentId) {
        if (request == null) {
            throw ApiErrors.badRequest("Artist details are required.");
        }
        String name = ApiErrors.requiredText(request.getArtistName(), "Artist name", 255);
        ApiErrors.optionalText(request.getGenre(), "Genre", 100);
        ApiErrors.optionalText(request.getCountry(), "Country", 100);
        ApiErrors.optionalText(request.getBio(), "Biography", 1000);
        boolean taken = artistRepository.findByArtistNameContainingIgnoreCase(name).stream()
                .anyMatch(a -> a.getArtistName() != null && a.getArtistName().trim().equalsIgnoreCase(name)
                        && (currentId == null || !a.getArtistId().equals(currentId)));
        if (taken) {
            throw ApiErrors.conflict("An artist with this name already exists.");
        }
    }

    private void apply(Artist artist, ArtistRequest request) {
        artist.setArtistName(request.getArtistName().trim());
        artist.setBio(ApiErrors.optionalText(request.getBio(), "Biography", 1000));
        artist.setCountry(ApiErrors.optionalText(request.getCountry(), "Country", 100));
        artist.setGenre(ApiErrors.optionalText(request.getGenre(), "Genre", 100));
    }

    private ArtistResponse convertToResponse(Artist artist) {
        return new ArtistResponse(
                artist.getArtistId(),
                artist.getArtistName(),
                artist.getBio(),
                artist.getCountry(),
                artist.getGenre(),
                artist.getImageUrl()
        );
    }
}
