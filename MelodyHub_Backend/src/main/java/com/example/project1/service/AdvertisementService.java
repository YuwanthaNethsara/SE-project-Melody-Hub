package com.example.project1.service;

import com.example.project1.dto.AdvertisementRequest;
import com.example.project1.dto.AdvertisementResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface AdvertisementService {

    /** JSON variant: keeps the old contract (imageUrl in the body). */
    AdvertisementResponse createAdvertisement(AdvertisementRequest request);

    /** Multipart variant: the picture is uploaded together with the data. */
    AdvertisementResponse createAdvertisement(AdvertisementRequest request, MultipartFile image);

    List<AdvertisementResponse> getAllAdvertisements();

    /** ACTIVE advertisements of ACTIVE campaigns, for the public store front. */
    List<AdvertisementResponse> getActiveAdvertisements();

    AdvertisementResponse getAdvertisementById(Long id);

    List<AdvertisementResponse> getAdvertisementsByCampaign(Long campaignId);

    List<AdvertisementResponse> searchAdvertisements(String title);

    AdvertisementResponse updateAdvertisement(Long id, AdvertisementRequest request);

    AdvertisementResponse updateAdvertisement(Long id, AdvertisementRequest request, MultipartFile image);

    String deleteAdvertisement(Long id);
}
