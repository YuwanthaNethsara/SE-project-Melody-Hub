package com.example.project1.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class CampaignResponse {

    private Long campaignId;
    private String campaignName;
    private String description;
    private java.math.BigDecimal budget;
    private LocalDate startDate;
    private LocalDate endDate;
    private String status;
    private LocalDateTime createdDate;

    public java.math.BigDecimal getBudget() {
        return budget;
    }

    public void setBudget(java.math.BigDecimal budget) {
        this.budget = budget;
    }

    public CampaignResponse() {
    }

    public CampaignResponse(
            Long campaignId,
            String campaignName,
            String description,
            LocalDate startDate,
            LocalDate endDate,
            String status,
            LocalDateTime createdDate
    ) {
        this.campaignId = campaignId;
        this.campaignName = campaignName;
        this.description = description;
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = status;
        this.createdDate = createdDate;
    }

    public Long getCampaignId() {
        return campaignId;
    }

    public void setCampaignId(Long campaignId) {
        this.campaignId = campaignId;
    }

    public String getCampaignName() {
        return campaignName;
    }

    public void setCampaignName(String campaignName) {
        this.campaignName = campaignName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }
}
