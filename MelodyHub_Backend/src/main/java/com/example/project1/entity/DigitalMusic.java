package com.example.project1.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "digital_music")
public class DigitalMusic {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long digitalMusicId;

    @OneToOne
    @JoinColumn(name = "song_id", nullable = false, unique = true)
    private Song song;

    @Column(nullable = false)
    private String fileName;

    @Column(name = "file_url", nullable = false, length = 500)
    private String fileUrl;

    @Column(name = "file_path", nullable = false, length = 255)
    private String filePath;

    private String fileFormat;

    private Long fileSize;

    private LocalDateTime uploadedAt;

    private String status;

    public DigitalMusic() {
    }

    public Long getDigitalMusicId() {
        return digitalMusicId;
    }

    public void setDigitalMusicId(Long digitalMusicId) {
        this.digitalMusicId = digitalMusicId;
    }

    public Song getSong() {
        return song;
    }

    public void setSong(Song song) {
        this.song = song;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileUrl() {
        return fileUrl;
    }

    public void setFileUrl(String fileUrl) {
        this.fileUrl = fileUrl;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getFileFormat() {
        return fileFormat;
    }

    public void setFileFormat(String fileFormat) {
        this.fileFormat = fileFormat;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    public LocalDateTime getUploadedAt() {
        return uploadedAt;
    }

    public void setUploadedAt(LocalDateTime uploadedAt) {
        this.uploadedAt = uploadedAt;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}