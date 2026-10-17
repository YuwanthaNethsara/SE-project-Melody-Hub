package com.example.project1.dto;

public class ArtistResponse {

    private Long artistId;
    private String artistName;
    private String bio;
    private String country;
    private String genre;
    private String imageUrl;

    public ArtistResponse() {
    }

    public ArtistResponse(
            Long artistId,
            String artistName,
            String bio,
            String country,
            String genre,
            String imageUrl) {

        this.artistId = artistId;
        this.artistName = artistName;
        this.bio = bio;
        this.country = country;
        this.genre = genre;
        this.imageUrl = imageUrl;
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

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
