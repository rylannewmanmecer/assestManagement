package com.example.assestmanagement.controller;

import com.example.assestmanagement.model.Asset;
import com.example.assestmanagement.repository.AssetRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.stream.Collectors;

@Controller
public class ManagerController {

    @Autowired
    private AssetRepository assetRepository;

    @GetMapping("/manager")
    public String managerDashboard(Model model) {
        List<Asset> allAssets = assetRepository.findAll();

        // Filter data arrays contextually for manager consumption
        List<Asset> filteringQueue = allAssets.stream()
                .filter(a -> "Pending Approval".equalsIgnoreCase(a.getStatus()) || "Checked Out".equalsIgnoreCase(a.getStatus()))
                .collect(Collectors.toList());

        long pendingCount = allAssets.stream().filter(a -> "Pending Approval".equalsIgnoreCase(a.getStatus())).count();
        long activeLoansCount = allAssets.stream().filter(a -> "Checked Out".equalsIgnoreCase(a.getStatus())).count();

        // Model attributes binding
        model.addAttribute("managedAssets", filteringQueue);
        model.addAttribute("pendingCount", pendingCount);
        model.addAttribute("activeLoansCount", activeLoansCount);

        return "manager";
    }

    @PostMapping("/manager/assets/approve")
    public String approveRequest(@RequestParam("assetId") String assetId) {
        Asset asset = assetRepository.findById(assetId).orElse(null);
        if (asset != null && "Pending Approval".equalsIgnoreCase(asset.getStatus())) {
            asset.setStatus("Checked Out");
            assetRepository.save(asset);
        }
        return "redirect:/manager";
    }

    @PostMapping("/manager/assets/deny")
    public String denyRequest(@RequestParam("assetId") String assetId) {
        Asset asset = assetRepository.findById(assetId).orElse(null);
        if (asset != null && "Pending Approval".equalsIgnoreCase(asset.getStatus())) {
            asset.setStatus("Available");
            asset.setRequestedBy(null);
            asset.setRequestedDurationDays(null); // Clear the requested days configuration window
            assetRepository.save(asset);
        }
        return "redirect:/manager";
    }

}