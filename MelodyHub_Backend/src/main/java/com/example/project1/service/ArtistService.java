package com.example.project1.service;

import com.example.project1.dto.ArtistRequest;
import com.example.project1.dto.ArtistResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ArtistService {

    ArtistResponse createArtist(ArtistRequest request);

    /** Multipart variant: the artist photo is uploaded together with the data. */
    ArtistResponse createArtist(ArtistRequest request, MultipartFile image);

    List<ArtistResponse> getAllArtists();

    ArtistResponse getArtistById(Long id);

    ArtistResponse updateArtist(Long id, ArtistRequest request);

    ArtistResponse updateArtist(Long id, ArtistRequest request, MultipartFile image);

    /** Uploads or replaces only the artist photo (the old file is deleted after the save). */
    ArtistResponse updateArtistImage(Long id, MultipartFile image);

    /** Removes the artist photo and its file. */
    ArtistResponse removeArtistImage(Long id);

    String deleteArtist(Long id);

    List<ArtistResponse> searchArtists(String name);
}
