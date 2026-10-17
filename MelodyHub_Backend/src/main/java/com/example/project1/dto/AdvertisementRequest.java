package com.example.project1.dto;

/**
 * Body of POST/PUT /api/advertisements.
 * With multipart requests this object is the JSON part called "data" and the picture is a
 * separate file part called "image". {@code removeImage=true} clears the stored picture.
 */
public class AdvertisementRequest {

    private String title;
    private String content;
    // only used by the JSON (non-multipart) endpoints; uploads go through the "image" file part
    private String imageUrl;
    private String status;
    private Long campaignId;
    private Boolean removeImage;

    public AdvertisementRequest() {
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Long getCampaignId() {
        return campaignId;
    }

    public void setCampaignId(Long campaignId) {
        this.campaignId = campaignId;
    }

    public Boolean getRemoveImage() {
        return removeImage;
    }

    public void setRemoveImage(Boolean removeImage) {
        this.removeImage = removeImage;
    }
}
