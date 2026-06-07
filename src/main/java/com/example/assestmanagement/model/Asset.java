package com.example.assestmanagement.model;

import jakarta.persistence.*;

@Entity
@Table(name = "assets")
public class Asset {


    @Column(name = "requested_duration_days")
    private Integer requestedDurationDays;

    @Id
    @Column(name = "asset_id", length = 50)
    private String assetId; // Changed from Long to String (True Primary Key)

    @Column(name = "asset_name", length = 100)
    private String assetName;

    @Column(name = "category", length = 100)
    private String category;

    @Column(name = "location", length = 100)
    private String location;

    @Column(name = "status", length = 50)
    private String status = "Available";

    @Column(name = "image_path", length = 255)
    private String imagePath = "/images/default-asset.png";

    @Column(name = "asset_type", length = 50)
    private String assetType = "ASSET";

    // Add this field if it is missing
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requested_by_user_id")
    private User requestedBy;

    // Make sure this exact setter is available for the ManagerController to use
    public void setRequestedBy(User requestedBy) {
        this.requestedBy = requestedBy;
    }

    // Ensure you also have the getter for rendering the template
    public User getRequestedBy() {
        return requestedBy;
    }

    public Asset() {}

    // Getters and Setters
    public String getAssetId() { return assetId; }
    public void setAssetId(String assetId) { this.assetId = assetId; }

    public String getAssetName() { return assetName; }
    public void setAssetName(String assetName) { this.assetName = assetName; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getImagePath() { return imagePath; }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }

    public String getAssetType() { return assetType; }
    public void setAssetType(String assetType) { this.assetType = assetType; }

    public Integer getRequestedDurationDays() { return requestedDurationDays; }
    public void setRequestedDurationDays(Integer requestedDurationDays) { this.requestedDurationDays = requestedDurationDays; }


    // Helper method to retrieve the correct prefix mapping string
    public String generatePrefix() {
        if (this.assetType == null) return "AST-";
        switch (this.assetType.toUpperCase()) {
            case "ASSET": return "IT-";
            case "LICENSE": return "LIC-";
            case "ACCESSORY": return "ACC-";
            case "CONSUMABLE": return "CON-";
            default: return "AST-";
        }

    }
}