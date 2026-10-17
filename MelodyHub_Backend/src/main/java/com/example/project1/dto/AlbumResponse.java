package com.example.project1.dto;

import java.time.LocalDate;

public class AlbumResponse {

    private Long albumId;
    private String title;
    private LocalDate releaseDate;
    private String description;
    private String coverImageUrl;

    private Long artistId;
    private String artistName;
    private String artistImageUrl;

    public AlbumResponse() {
    }

    public AlbumResponse(
            Long albumId,
            String title,
            LocalDate releaseDate,
            String description,
            String coverImageUrl,
            Long artistId,
            String artistName) {

        this.albumId = albumId;
        this.title = title;
        this.releaseDate = releaseDate;
        this.description = description;
        this.coverImageUrl = coverImageUrl;
        this.artistId = artistId;
        this.artistName = artistName;
    }

    public Long getAlbumId() {
        return albumId;
    }

    public void setAlbumId(Long albumId) {
        this.albumId = albumId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public LocalDate getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(LocalDate releaseDate) {
        this.releaseDate = releaseDate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCoverImageUrl() {
        return coverImageUrl;
    }

    public void setCoverImageUrl(String coverImageUrl) {
        this.coverImageUrl = coverImageUrl;
    }

    public Long getArtistId() {
        return artistId;
    }

    public void setArtistId(Long artistId) {
        this.artistId = artistId;
    }

    public String getArtistName() {
        return artistName;
    }

    public void setArtistName(String artistName) {
        this.artistName = artistName;
    }

    /** Public URL of the artist photo (null = show the default picture). */
    public String getArtistImageUrl() {
        return artistImageUrl;
    }

    public void setArtistImageUrl(String artistImageUrl) {
        this.artistImageUrl = artistImageUrl;
    }
}
