package com.example.assestmanagement.repository;

import com.example.assestmanagement.model.Asset;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AssetRepository extends JpaRepository<Asset, String> {
    // Standard CRUD methods (save, findAll, count) are provided automatically
}