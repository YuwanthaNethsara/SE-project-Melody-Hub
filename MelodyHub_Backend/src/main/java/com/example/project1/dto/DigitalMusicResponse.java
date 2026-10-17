package com.example.project1.dto;

import java.time.LocalDateTime;

/**
 * Digital music as returned by the API. The server-side file path is never exposed:
 * {@code fileUrl} is the authenticated download address (/api/digital-music/{id}/download).
 */
public class DigitalMusicResponse {
    private Long digitalMusicId;
    private Long songId;
    private String songTitle;
    private String duration;
    private Long albumId;
    private String albumTitle;
    private String coverImageUrl;
    private Long artistId;
    private String artistName;
    private String artistImageUrl;
    private String fileName;
    private String fileUrl;
    private String fileFormat;
    private Long fileSize;
    private LocalDateTime uploadedAt;
    private String status;

    public DigitalMusicResponse() {
    }

    public Long getDigitalMusicId() { return digitalMusicId; }
    public void setDigitalMusicId(Long v) { digitalMusicId = v; }
    public Long getSongId() { return songId; }
    public void setSongId(Long v) { songId = v; }
    public String getSongTitle() { return songTitle; }
    public void setSongTitle(String v) { songTitle = v; }
    public String getDuration() { return duration; }
    public void setDuration(String v) { duration = v; }
    public Long getAlbumId() { return albumId; }
    public void setAlbumId(Long v) { albumId = v; }
    public String getAlbumTitle() { return albumTitle; }
    public void setAlbumTitle(String v) { albumTitle = v; }
    public String getCoverImageUrl() { return coverImageUrl; }
    public void setCoverImageUrl(String v) { coverImageUrl = v; }
    public Long getArtistId() { return artistId; }
    public void setArtistId(Long v) { artistId = v; }
    public String getArtistName() { return artistName; }
    public void setArtistName(String v) { artistName = v; }
    public String getArtistImageUrl() { return artistImageUrl; }
    public void setArtistImageUrl(String v) { artistImageUrl = v; }
    public String getFileName() { return fileName; }
    public void setFileName(String v) { fileName = v; }
    public String getFileUrl() { return fileUrl; }
    public void setFileUrl(String v) { fileUrl = v; }
    public String getFileFormat() { return fileFormat; }
    public void setFileFormat(String v) { fileFormat = v; }
    public Long getFileSize() { return fileSize; }
    public void setFileSize(Long v) { fileSize = v; }
    public LocalDateTime getUploadedAt() { return uploadedAt; }
    public void setUploadedAt(LocalDateTime v) { uploadedAt = v; }
    public String getStatus() { return status; }
    public void setStatus(String v) { status = v; }
}
