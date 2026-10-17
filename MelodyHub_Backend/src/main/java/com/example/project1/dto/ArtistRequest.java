package com.example.project1.dto;

public class ArtistRequest {

    private String artistName;
    private String bio;
    private String country;
    private String genre;
    private String imageUrl;

    // multipart updates: true clears the stored photo
    private Boolean removeImage;

    public ArtistRequest() {
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

    public Boolean getRemoveImage() {
        return removeImage;
    }

    public void setRemoveImage(Boolean removeImage) {
        this.removeImage = removeImage;
    }
}
