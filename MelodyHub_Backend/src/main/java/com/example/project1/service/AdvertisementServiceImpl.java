package com.example.project1.service;

import com.example.project1.dto.AdvertisementRequest;
import com.example.project1.dto.AdvertisementResponse;
import com.example.project1.entity.Advertisement;
import com.example.project1.entity.Campaign;
import com.example.project1.exception.ApiErrors;
import com.example.project1.repository.AdvertisementRepository;
import com.example.project1.repository.CampaignRepository;
import com.example.project1.storage.FileStorageService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Advertisements with a real uploaded picture.
 *
 * Image life cycle (no orphan files):
 *  - create: the picture is stored, then the row is saved; if the save fails the new file is deleted.
 *  - update: a new picture replaces the old one; the old file is deleted only AFTER the row is saved.
 *    {@code removeImage=true} clears the picture and deletes the file.
 *  - delete: the row is deleted, then its picture file.
 * Repository calls are not wrapped in one transaction on purpose: each save is committed before
 * an old file is removed, so a failed save can never lose the picture that is still referenced.
 */
@Service
public class AdvertisementServiceImpl implements AdvertisementService {

    private static final Set<String> STATUSES = Set.of("ACTIVE", "PAUSED", "ARCHIVED");

    private final AdvertisementRepository advertisementRepository;
    private final CampaignRepository campaignRepository;
    private final FileStorageService storage;

    public AdvertisementServiceImpl(AdvertisementRepository advertisementRepository,
                                    CampaignRepository campaignRepository,
                                    FileStorageService storage) {
        this.advertisementRepository = advertisementRepository;
        this.campaignRepository = campaignRepository;
        this.storage = storage;
    }

    // ------------------------------------------------------------------ create

    @Override
    public AdvertisementResponse createAdvertisement(AdvertisementRequest request) {
        validateRequest(request);
        String url = storage.resolveJsonImageUrl(null, request.getImageUrl(),
                Boolean.TRUE.equals(request.getRemoveImage()), 500);
        return create(request, url);
    }

    @Override
    public AdvertisementResponse createAdvertisement(AdvertisementRequest request, MultipartFile image) {
        validateRequest(request);
        Campaign campaign = findCampaign(request.getCampaignId());
        requireUniqueTitle(request.getTitle().trim(), campaign.getCampaignId(), null);
        String newUrl = hasFile(image) ? storage.storeImage(image, FileStorageService.ADVERTISEMENTS) : null;
        try {
            return create(request, newUrl);
        } catch (RuntimeException e) {
            storage.deleteImageQuietly(newUrl);
            throw e;
        }
    }

    private AdvertisementResponse create(AdvertisementRequest request, String imageUrl) {
        Campaign campaign = findCampaign(request.getCampaignId());
        requireUniqueTitle(request.getTitle().trim(), campaign.getCampaignId(), null);
        Advertisement ad = new Advertisement();
        apply(ad, request, campaign);
        ad.setImageUrl(imageUrl);
        ad.setCreatedDate(LocalDateTime.now());
        return convertToResponse(advertisementRepository.save(ad));
    }

    // ------------------------------------------------------------------ read

    @Override
    public List<AdvertisementResponse> getAllAdvertisements() {
        return advertisementRepository.findAll().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<AdvertisementResponse> getActiveAdvertisements() {
        LocalDate today = LocalDate.now();
        return advertisementRepository.findByStatus("ACTIVE").stream()
                .filter(ad -> ad.getCampaign() != null && campaignRunsOn(ad.getCampaign(), today))
                .sorted(Comparator.comparing(Advertisement::getCreatedDate,
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public AdvertisementResponse getAdvertisementById(Long id) {
        return convertToResponse(find(id));
    }

    @Override
    public List<AdvertisementResponse> getAdvertisementsByCampaign(Long campaignId) {
        return advertisementRepository.findByCampaign_CampaignId(campaignId).stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<AdvertisementResponse> searchAdvertisements(String title) {
        return advertisementRepository.findByTitleContainingIgnoreCase(title == null ? "" : title.trim()).stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    // ------------------------------------------------------------------ update

    @Override
    public AdvertisementResponse updateAdvertisement(Long id, AdvertisementRequest request) {
        Advertisement ad = find(id);
        validateRequest(request);
        String oldUrl = ad.getImageUrl();
        String newUrl = storage.resolveJsonImageUrl(oldUrl, request.getImageUrl(),
                Boolean.TRUE.equals(request.getRemoveImage()), 500);
        Campaign campaign = findCampaign(request.getCampaignId());
        requireUniqueTitle(request.getTitle().trim(), campaign.getCampaignId(), id);
        apply(ad, request, campaign);
        ad.setImageUrl(newUrl);
        AdvertisementResponse saved = convertToResponse(advertisementRepository.save(ad));
        if (!Objects.equals(oldUrl, newUrl)) {
            storage.deleteImageQuietly(oldUrl);
        }
        return saved;
    }

    @Override
    public AdvertisementResponse updateAdvertisement(Long id, AdvertisementRequest request, MultipartFile image) {
        Advertisement ad = find(id);
        validateRequest(request);
        Campaign campaign = findCampaign(request.getCampaignId());
        requireUniqueTitle(request.getTitle().trim(), campaign.getCampaignId(), id);

        String oldUrl = ad.getImageUrl();
        String newUrl = oldUrl;
        boolean uploaded = hasFile(image);
        if (uploaded) {
            newUrl = storage.storeImage(image, FileStorageService.ADVERTISEMENTS);
        } else if (Boolean.TRUE.equals(request.getRemoveImage())) {
            newUrl = null;
        }

        apply(ad, request, campaign);
        ad.setImageUrl(newUrl);
        AdvertisementResponse saved;
        try {
            saved = convertToResponse(advertisementRepository.save(ad));
        } catch (RuntimeException e) {
            if (uploaded) {
                storage.deleteImageQuietly(newUrl); // the old picture is still referenced, keep it
            }
            throw e;
        }
        if (!Objects.equals(oldUrl, newUrl)) {
            storage.deleteImageQuietly(oldUrl);
        }
        return saved;
    }

    // ------------------------------------------------------------------ delete

    @Override
    public String deleteAdvertisement(Long id) {
        Advertisement ad = find(id);
        String imageUrl = ad.getImageUrl();
        advertisementRepository.delete(ad);
        storage.deleteImageQuietly(imageUrl);
        return "Advertisement deleted successfully";
    }

    // ------------------------------------------------------------------ helpers

    private Advertisement find(Long id) {
        return advertisementRepository.findById(ApiErrors.required(id, "Advertisement id is required."))
                .orElseThrow(() -> ApiErrors.notFound("Advertisement not found."));
    }

    private Campaign findCampaign(Long campaignId) {
        return campaignRepository.findById(ApiErrors.required(campaignId, "Please choose a campaign."))
                .orElseThrow(() -> ApiErrors.notFound("The selected campaign was not found."));
    }

    private void requireUniqueTitle(String title, Long campaignId, Long currentId) {
        boolean taken = advertisementRepository.findByCampaign_CampaignId(campaignId).stream()
                .filter(other -> currentId == null || !other.getAdvertisementId().equals(currentId))
                .anyMatch(other -> other.getTitle() != null && other.getTitle().equalsIgnoreCase(title));
        if (taken) {
            throw ApiErrors.conflict("An advertisement with this title already exists in this campaign.");
        }
    }

    private static boolean hasFile(MultipartFile file) {
        return file != null && !file.isEmpty();
    }

    private static boolean campaignRunsOn(Campaign campaign, LocalDate day) {
        boolean started = campaign.getStartDate() == null || !day.isBefore(campaign.getStartDate());
        boolean notEnded = campaign.getEndDate() == null || !day.isAfter(campaign.getEndDate());
        return started && notEnded;
    }

    private void apply(Advertisement ad, AdvertisementRequest request, Campaign campaign) {
        ad.setTitle(request.getTitle().trim());
        ad.setContent(request.getContent().trim());
        ad.setStatus(request.getStatus() == null || request.getStatus().isBlank()
                ? "ACTIVE" : request.getStatus().trim().toUpperCase(Locale.ROOT));
        ad.setCampaign(campaign);
    }

    private void validateRequest(AdvertisementRequest request) {
        if (request == null) {
            throw ApiErrors.badRequest("Advertisement details are required.");
        }
        String title = ApiErrors.requiredText(request.getTitle(), "Advertisement title", 150);
        if (title.length() < 2) {
            throw ApiErrors.badRequest("Advertisement title must be at least 2 characters.");
        }
        ApiErrors.requiredText(request.getContent(), "Advertisement content", 1000);
        ApiErrors.required(request.getCampaignId(), "Please choose a campaign.");
        if (request.getStatus() != null && !request.getStatus().isBlank()
                && !STATUSES.contains(request.getStatus().trim().toUpperCase(Locale.ROOT))) {
            throw ApiErrors.badRequest("Status must be ACTIVE, PAUSED or ARCHIVED.");
        }
    }

    private AdvertisementResponse convertToResponse(Advertisement ad) {
        Campaign campaign = ad.getCampaign();
        AdvertisementResponse response = new AdvertisementResponse(
                ad.getAdvertisementId(),
                ad.getTitle(),
                ad.getContent(),
                ad.getImageUrl(),
                ad.getStatus(),
                ad.getCreatedDate(),
                campaign != null ? campaign.getCampaignId() : null,
                campaign != null ? campaign.getCampaignName() : null);
        if (campaign != null) {
            response.setCampaignStatus(campaign.getStatus());
        }
        return response;
    }
}
