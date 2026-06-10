package com.example.assestmanagement.controller;

import com.example.assestmanagement.model.Asset;
import com.example.assestmanagement.repository.AssetRepository;
import com.example.assestmanagement.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import java.util.UUID;

@Controller
public class AdminController {

    @Autowired
    private AssetRepository assetRepository;

    @Autowired
    private UserRepository userRepository;

    private static final String UPLOAD_DIR = System.getProperty("user.dir") + "/uploads/";

    @GetMapping("/admin")
    public String adminPage(Model model) {
        List<Asset> allItems = assetRepository.findAll();
        long totalPeople = userRepository.count();

        // Calculate segregated totals for your dashboard view metric cards
        long assetsCount = allItems.stream().filter(i -> "ASSET".equals(i.getAssetType())).count();
        long licensesCount = allItems.stream().filter(i -> "LICENSE".equals(i.getAssetType())).count();
        long accessoriesCount = allItems.stream().filter(i -> "ACCESSORY".equals(i.getAssetType())).count();
        long consumablesCount = allItems.stream().filter(i -> "CONSUMABLE".equals(i.getAssetType())).count();

        model.addAttribute("assets", allItems);
        model.addAttribute("totalAssets", assetsCount);
        model.addAttribute("totalLicenses", licensesCount);
        model.addAttribute("totalAccessories", accessoriesCount);
        model.addAttribute("totalConsumables", consumablesCount);
        model.addAttribute("totalPeople", totalPeople == 0 ? 1 : totalPeople);

        model.addAttribute("newAsset", new Asset());
        return "admin";
    }

    @PostMapping("/admin/assets/save")
    public String saveAsset(@ModelAttribute("newAsset") Asset asset,
                            @RequestParam("imageFile") MultipartFile file) {

        System.out.println("--- DEBUG ASSET SAVE ---");
        System.out.println("Received Asset ID: '" + asset.getAssetId() + "'");
        System.out.println("Received Asset Type: '" + asset.getAssetType() + "'");

        // 1. Robust check for blank or null ID strings
        boolean isNew = (asset.getAssetId() == null || asset.getAssetId().trim().isEmpty() || asset.getAssetId().equals("null"));

        if (!isNew) {
            // Updating an existing record
            Asset existing = assetRepository.findById(asset.getAssetId()).orElse(null);
            if (existing != null && file.isEmpty()) {
                asset.setImagePath(existing.getImagePath());
            }
        } else {
            // 2. Generating a clean prefix safely
            String prefix = "AST-";
            if (asset.getAssetType() != null && !asset.getAssetType().trim().isEmpty()) {
                prefix = asset.generatePrefix();
            }

            // 3. Fallback tracking logic in case findAll() stream errors out
            long countInSameCategory = 0;
            try {
                List<Asset> allItems = assetRepository.findAll();
                if (allItems != null) {
                    final String targetPrefix = prefix;
                    countInSameCategory = allItems.stream()
                            .filter(item -> item != null && item.getAssetId() != null && item.getAssetId().startsWith(targetPrefix))
                            .count();
                }
            } catch (Exception e) {
                System.out.println("Stream counter fallback triggered: " + e.getMessage());
            }

            String generatedCustomStringKey = prefix + (1001 + countInSameCategory);
            System.out.println("Generated New Primary Key: " + generatedCustomStringKey);
            asset.setAssetId(generatedCustomStringKey);
        }

        if (asset.getStatus() == null || asset.getStatus().isEmpty()) {
            asset.setStatus("Available");
        }

        // Image file handler
        if (!file.isEmpty()) {
            try {
                Path uploadPath = Paths.get(UPLOAD_DIR);
                if (!Files.exists(uploadPath)) {
                    Files.createDirectories(uploadPath);
                }
                String uniqueFilename = UUID.randomUUID() + "_" + file.getOriginalFilename();
                Path filePath = uploadPath.resolve(uniqueFilename);
                Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
                asset.setImagePath("/images/" + uniqueFilename);
            } catch (IOException e) {
                System.out.println("Image upload failed: " + e.getMessage());
                if (asset.getImagePath() == null) asset.setImagePath("/images/default-asset.png");
            }
        } else if (asset.getImagePath() == null || asset.getImagePath().isEmpty()) {
            if (isNew) {
                asset.setImagePath("/images/default-asset.png");
            }
        }

        System.out.println("Attempting database save for ID: " + asset.getAssetId());
        assetRepository.save(asset);
        System.out.println("Save successful!");

        return "redirect:/admin";
    }

    // Updated API endpoint parameter receiver format changing Long identifier maps to Strings
    @GetMapping("/admin/assets/api/{id}")
    @ResponseBody
    public Asset getAssetDetails(@PathVariable("id") String id) {
        return assetRepository.findById(id).orElse(new Asset());
    }

    // Updated Delete parameters processing custom String values
    @PostMapping("/admin/assets/delete")
    public String deleteAsset(@RequestParam("deleteId") String id) {
        assetRepository.deleteById(id);
        return "redirect:/admin";
    }
}

