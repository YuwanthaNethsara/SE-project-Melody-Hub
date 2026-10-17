package com.example.project1.service;

import com.example.project1.dto.SongRequest;
import com.example.project1.dto.SongResponse;

import java.util.List;

public interface SongService {

    SongResponse createSong(SongRequest request);

    List<SongResponse> getAllSongs();

    SongResponse getSongById(Long id);

    SongResponse updateSong(Long id, SongRequest request);

    String deleteSong(Long id);

    List<SongResponse> getSongsByAlbum(Long albumId);

    List<SongResponse> searchSongs(String title);
}
