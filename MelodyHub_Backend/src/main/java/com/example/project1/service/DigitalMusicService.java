package com.example.project1.service;

import com.example.project1.dto.DigitalMusicResponse;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.util.List;

public interface DigitalMusicService {

    /** A file that is ready to be streamed to the browser. */
    record MusicDownload(Path path, String downloadName, long size) {
    }

    DigitalMusicResponse uploadMusic(Long songId, MultipartFile file, String status);

    /** {@code includeInactive=false} returns only tracks customers may download. */
    List<DigitalMusicResponse> getAllMusic(boolean includeInactive);

    DigitalMusicResponse getMusicById(Long id, boolean includeInactive);

    DigitalMusicResponse getMusicBySong(Long songId, boolean includeInactive);

    DigitalMusicResponse updateStatus(Long id, String status);

    String deleteMusic(Long id);

    /** Checks availability and that the MP3 still exists on disk. Managers may download inactive tracks. */
    MusicDownload prepareDownload(Long id, boolean manager);
}
