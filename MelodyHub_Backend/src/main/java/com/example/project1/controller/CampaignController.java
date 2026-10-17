package com.example.project1.controller;

import com.example.project1.dto.CampaignRequest;
import com.example.project1.dto.CampaignResponse;
import com.example.project1.service.CampaignService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/campaigns")
public class CampaignController {

    private final CampaignService campaignService;

    public CampaignController(
            CampaignService campaignService
    ) {
        this.campaignService = campaignService;
    }

    // CREATE
    @PostMapping
    public ResponseEntity<CampaignResponse>
    createCampaign(
            @RequestBody CampaignRequest request
    ) {

        return new ResponseEntity<>(
                campaignService.createCampaign(request),
                HttpStatus.CREATED
        );
    }

    // GET ALL
    @GetMapping
    public ResponseEntity<List<CampaignResponse>>
    getAllCampaigns() {

        return ResponseEntity.ok(
                campaignService.getAllCampaigns()
        );
    }

    // GET ACTIVE
    @GetMapping("/active")
    public ResponseEntity<List<CampaignResponse>>
    getActiveCampaigns() {

        return ResponseEntity.ok(
                campaignService.getActiveCampaigns()
        );
    }

    // SEARCH
    @GetMapping("/search")
    public ResponseEntity<List<CampaignResponse>>
    searchCampaigns(
            @RequestParam String name
    ) {

        return ResponseEntity.ok(
                campaignService.searchCampaigns(name)
        );
    }

    // GET BY ID
    @GetMapping("/{id}")
    public ResponseEntity<CampaignResponse>
    getCampaignById(
            @PathVariable Long id
    ) {

        return ResponseEntity.ok(
                campaignService.getCampaignById(id)
        );
    }

    // UPDATE
    @PutMapping("/{id}")
    public ResponseEntity<CampaignResponse>
    updateCampaign(
            @PathVariable Long id,
            @RequestBody CampaignRequest request
    ) {

        return ResponseEntity.ok(
                campaignService.updateCampaign(
                        id,
                        request
                )
        );
    }

    // DELETE
    @DeleteMapping("/{id}")
    public ResponseEntity<String>
    deleteCampaign(
            @PathVariable Long id
    ) {

        return ResponseEntity.ok(
                campaignService.deleteCampaign(id)
        );
    }
}
