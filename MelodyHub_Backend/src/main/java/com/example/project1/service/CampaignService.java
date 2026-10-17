package com.example.project1.service;

import com.example.project1.dto.CampaignRequest;
import com.example.project1.dto.CampaignResponse;

import java.util.List;

public interface CampaignService {

    CampaignResponse createCampaign(
            CampaignRequest request
    );

    List<CampaignResponse> getAllCampaigns();

    List<CampaignResponse> getActiveCampaigns();

    CampaignResponse getCampaignById(Long id);

    List<CampaignResponse> searchCampaigns(
            String name
    );

    CampaignResponse updateCampaign(
            Long id,
            CampaignRequest request
    );

    String deleteCampaign(Long id);
}
