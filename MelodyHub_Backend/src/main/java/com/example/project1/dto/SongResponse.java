package com.example.project1.dto;

public class SongResponse {

    private Long songId;
    private String title;
    private String duration;
    private Integer trackNumber;

    private Long albumId;
    private String albumTitle;

    private Long artistId;
    private String artistName;
    private String artistImageUrl;

    public SongResponse() {
    }

    public SongResponse(
            Long songId,
            String title,
            String duration,
            Integer trackNumber,
            Long albumId,
            String albumTitle,
            Long artistId,
            String artistName) {

        this.songId = songId;
        this.title = title;
        this.duration = duration;
        this.trackNumber = trackNumber;
        this.albumId = albumId;
        this.albumTitle = albumTitle;
        this.artistId = artistId;
        this.artistName = artistName;
    }

    public Long getSongId() {
        return songId;
    }

    public void setSongId(Long songId) {
        this.songId = songId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public Integer getTrackNumber() {
        return trackNumber;
    }

    public void setTrackNumber(Integer trackNumber) {
        this.trackNumber = trackNumber;
    }

    public Long getAlbumId() {
        return albumId;
    }

    public void setAlbumId(Long albumId) {
        this.albumId = albumId;
    }

    public String getAlbumTitle() {
        return albumTitle;
    }

    public void setAlbumTitle(String albumTitle) {
        this.albumTitle = albumTitle;
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
