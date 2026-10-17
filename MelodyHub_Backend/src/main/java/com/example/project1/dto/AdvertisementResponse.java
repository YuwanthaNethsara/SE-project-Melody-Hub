package com.example.project1.dto;

import java.time.LocalDateTime;

public class AdvertisementResponse {

    private Long advertisementId;
    private String title;
    private String content;
    private String imageUrl;
    private String status;
    private LocalDateTime createdDate;
    private Long campaignId;
    private String campaignName;
    private String campaignStatus;

    public AdvertisementResponse() {
    }

    public AdvertisementResponse(Long advertisementId, String title, String content, String imageUrl,
                                 String status, LocalDateTime createdDate,
                                 Long campaignId, String campaignName) {
        this.advertisementId = advertisementId;
        this.title = title;
        this.content = content;
        this.imageUrl = imageUrl;
        this.status = status;
        this.createdDate = createdDate;
        this.campaignId = campaignId;
        this.campaignName = campaignName;
    }

    public Long getAdvertisementId() {
        return advertisementId;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getStatus() {
        return status;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public Long getCampaignId() {
        return campaignId;
    }

    public String getCampaignName() {
        return campaignName;
    }

    public String getCampaignStatus() {
        return campaignStatus;
    }

    public void setCampaignStatus(String campaignStatus) {
        this.campaignStatus = campaignStatus;
    }
}
