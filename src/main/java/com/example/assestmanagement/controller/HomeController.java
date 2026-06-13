package com.example.assestmanagement.controller;

import com.example.assestmanagement.model.Asset;
import com.example.assestmanagement.model.User;
import com.example.assestmanagement.repository.AssetRepository;
import com.example.assestmanagement.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class HomeController {

    @Autowired
    private AssetRepository assetRepository;

    @Autowired
    private UserRepository userRepository;

    // FIX: This maps the "/home" URL path to prevent the 404 error page
    @GetMapping("/home")
    public String homePage(HttpSession session, Model model) {
        // Optional safety check: Ensure user is logged in
        String loggedInEmail = (String) session.getAttribute("loggedInUserEmail");
        if (loggedInEmail == null) {
            return "redirect:/"; // Redirect back to login page if session is missing
        }

        // Fetch all items from the database catalog
        List<Asset> assetsList = assetRepository.findAll();
        long totalAssetsCount = assetsList.size();

        // Pass data downstream to your thymeleaf template context
        model.addAttribute("assets", assetsList);
        model.addAttribute("totalAssets", totalAssetsCount);

        return "index"; // This loads your templates/index.html file
    }


    @PostMapping("/user/assets/request")
    public String requestAssetLoan(@RequestParam("assetId") String assetId,
                                   @RequestParam("loanDurationDays") Integer loanDurationDays,
                                   HttpSession session) {
        String loggedInEmail = (String) session.getAttribute("loggedInUserEmail");
        if (loggedInEmail == null) return "redirect:/";

        Asset asset = assetRepository.findById(assetId).orElse(null);
        User user = userRepository.findByEmail(loggedInEmail);

        if (asset != null && user != null && "Available".equalsIgnoreCase(asset.getStatus())) {
            asset.setStatus("Pending Approval");
            asset.setRequestedBy(user);
            asset.setRequestedDurationDays(loanDurationDays); // Store chosen schedule metric
            assetRepository.save(asset);
        }
        return "redirect:/home";
    }
}