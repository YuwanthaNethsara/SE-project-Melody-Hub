package com.example.project1.service;

import com.example.project1.dto.CampaignRequest;
import com.example.project1.dto.CampaignResponse;
import com.example.project1.entity.Campaign;
import com.example.project1.repository.AdvertisementRepository;
import com.example.project1.repository.CampaignRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CampaignServiceImpl
        implements CampaignService {

    private final CampaignRepository campaignRepository;
    private final AdvertisementRepository advertisementRepository;

    public CampaignServiceImpl(
            CampaignRepository campaignRepository,
            AdvertisementRepository advertisementRepository
    ) {
        this.advertisementRepository = advertisementRepository;
        this.campaignRepository = campaignRepository;
    }

    // CREATE
    @Override
    public CampaignResponse createCampaign(
            CampaignRequest request
    ) {

        validateRequest(request);

        Campaign campaign = new Campaign();

        campaign.setCampaignName(
                request.getCampaignName()
        );

        campaign.setDescription(
                request.getDescription()
        );

        campaign.setBudget(request.getBudget());

        campaign.setStartDate(
                request.getStartDate()
        );

        campaign.setEndDate(
                request.getEndDate()
        );

        campaign.setStatus(
                calculateStatus(
                        request.getStartDate(),
                        request.getEndDate()
                )
        );

        campaign.setCreatedDate(
                LocalDateTime.now()
        );

        Campaign saved =
                campaignRepository.save(campaign);

        return convertToResponse(saved);
    }

    // GET ALL
    @Override
    public List<CampaignResponse> getAllCampaigns() {

        updateExpiredCampaigns();

        return campaignRepository
                .findAll()
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    // GET ACTIVE
    @Override
    public List<CampaignResponse> getActiveCampaigns() {

        updateExpiredCampaigns();

        return campaignRepository
                .findByStatus("ACTIVE")
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    // GET BY ID
    @Override
    public CampaignResponse getCampaignById(
            Long id
    ) {

        Campaign campaign =
                campaignRepository
                        .findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Campaign not found"
                                )
                        );

        updateStatus(campaign);

        return convertToResponse(campaign);
    }

    // SEARCH
    @Override
    public List<CampaignResponse> searchCampaigns(
            String name
    ) {

        return campaignRepository
                .findByCampaignNameContainingIgnoreCase(name)
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    // UPDATE
    @Override
    public CampaignResponse updateCampaign(
            Long id,
            CampaignRequest request
    ) {

        validateRequest(request);

        Campaign campaign =
                campaignRepository
                        .findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Campaign not found"
                                )
                        );

        campaign.setCampaignName(
                request.getCampaignName()
        );

        campaign.setDescription(
                request.getDescription()
        );

        campaign.setBudget(request.getBudget());

        campaign.setStartDate(
                request.getStartDate()
        );

        campaign.setEndDate(
                request.getEndDate()
        );

        campaign.setStatus(
                calculateStatus(
                        request.getStartDate(),
                        request.getEndDate()
                )
        );

        Campaign updated =
                campaignRepository.save(campaign);

        return convertToResponse(updated);
    }

    // DELETE
    @Override
    public String deleteCampaign(Long id) {

        if (!campaignRepository.existsById(id)) {

            throw new RuntimeException(
                    "Campaign not found"
            );
        }

        if (advertisementRepository.existsByCampaign_CampaignId(id)) {

            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "This campaign still has advertisements. Delete those advertisements first.");
        }

        campaignRepository.deleteById(id);

        return "Campaign deleted successfully";
    }

    // VALIDATION
    private void validateRequest(
            CampaignRequest request
    ) {

        if (request.getCampaignName() == null ||
                request.getCampaignName().trim().isEmpty()) {

            throw new RuntimeException(
                    "Campaign name is required"
            );
        }

        if (request.getCampaignName().trim().length() > 255) {

            throw new IllegalArgumentException(
                    "Campaign name must be at most 255 characters"
            );
        }

        if (request.getDescription() != null
                && request.getDescription().length() > 1000) {

            throw new IllegalArgumentException(
                    "Description must be at most 1000 characters"
            );
        }

        if (request.getBudget() != null) {

            if (request.getBudget().signum() < 0) {

                throw new IllegalArgumentException(
                        "Budget cannot be negative"
                );
            }

            if (request.getBudget().compareTo(new java.math.BigDecimal("9999999999.99")) > 0) {

                throw new IllegalArgumentException(
                        "Budget is too large"
                );
            }
        }

        if (request.getStartDate() == null) {

            throw new RuntimeException(
                    "Start date is required"
            );
        }

        if (request.getEndDate() == null) {

            throw new RuntimeException(
                    "End date is required"
            );
        }

        if (request.getEndDate()
                .isBefore(request.getStartDate())) {

            throw new RuntimeException(
                    "End date cannot be before start date"
            );
        }
    }

    // STATUS
    private String calculateStatus(
            LocalDate startDate,
            LocalDate endDate
    ) {

        LocalDate today = LocalDate.now();

        if (today.isBefore(startDate)) {
            return "UPCOMING";
        }

        if (today.isAfter(endDate)) {
            return "EXPIRED";
        }

        return "ACTIVE";
    }

    // UPDATE STATUS
    private void updateStatus(
            Campaign campaign
    ) {

        String currentStatus =
                calculateStatus(
                        campaign.getStartDate(),
                        campaign.getEndDate()
                );

        if (!currentStatus.equals(
                campaign.getStatus())) {

            campaign.setStatus(currentStatus);

            campaignRepository.save(campaign);
        }
    }

    private void updateExpiredCampaigns() {

        List<Campaign> campaigns =
                campaignRepository.findAll();

        for (Campaign campaign : campaigns) {

            updateStatus(campaign);
        }
    }

    // CONVERT
    private CampaignResponse convertToResponse(
            Campaign campaign
    ) {

        CampaignResponse response = new CampaignResponse(

                campaign.getCampaignId(),

                campaign.getCampaignName(),

                campaign.getDescription(),

                campaign.getStartDate(),

                campaign.getEndDate(),

                campaign.getStatus(),

                campaign.getCreatedDate()
        );

        response.setBudget(campaign.getBudget());

        return response;
    }
}
