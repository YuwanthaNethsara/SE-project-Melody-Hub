package com.example.project1.repository;

import com.example.project1.entity.Advertisement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AdvertisementRepository extends JpaRepository<Advertisement, Long> {

    List<Advertisement> findByCampaign_CampaignId(Long campaignId);

    List<Advertisement> findByTitleContainingIgnoreCase(String title);

    boolean existsByCampaign_CampaignId(Long campaignId);

    List<Advertisement> findByStatus(String status);

    long countByStatus(String status);

    boolean existsByTitleIgnoreCaseAndCampaign_CampaignId(String title, Long campaignId);
}
