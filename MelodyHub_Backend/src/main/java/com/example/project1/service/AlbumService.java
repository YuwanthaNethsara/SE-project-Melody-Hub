package com.example.project1.service;

import com.example.project1.dto.AlbumRequest;
import com.example.project1.dto.AlbumResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface AlbumService {

    AlbumResponse createAlbum(AlbumRequest request);

    /** Multipart variant: the cover picture is uploaded together with the data. */
    AlbumResponse createAlbum(AlbumRequest request, MultipartFile image);

    List<AlbumResponse> getAllAlbums();

    AlbumResponse getAlbumById(Long id);

    AlbumResponse updateAlbum(Long id, AlbumRequest request);

    AlbumResponse updateAlbum(Long id, AlbumRequest request, MultipartFile image);

    String deleteAlbum(Long id);

    List<AlbumResponse> getAlbumsByArtist(Long artistId);

    List<AlbumResponse> searchAlbums(String title);
}
