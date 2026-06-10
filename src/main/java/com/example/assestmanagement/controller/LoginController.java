package com.example.assestmanagement.controller;

import com.example.assestmanagement.model.User;
import com.example.assestmanagement.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Optional;

@Controller
public class LoginController {

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/")
    public String loginPage() {
        return "login";
    }

    @PostMapping("/login")
    public String login(
            @RequestParam("email") String email,
            @RequestParam("password") String password,
            HttpSession session,
            Model model) {

        // Use the native crypto query from your UserRepository to check email and hashed password
        Optional<User> validUser = userRepository.checkCredentials(email, password);

        if (validUser.isPresent()) {
            User user = validUser.get();

            // Store the user identity context inside the HTTP session tracking layer
            session.setAttribute("loggedInUserEmail", user.getEmail());
            session.setAttribute("userRole", user.getRole());

            // Redirection Engine based on account authority privileges
            if ("ADMIN".equalsIgnoreCase(user.getRole())) {
                return "redirect:/admin";
            } else if ("MANAGER".equalsIgnoreCase(user.getRole())) {
                return "redirect:/manager";
            }

            // Fallback for standard consumer profiles
            return "redirect:/home";
        }

        // Failure state: Pass flags back to Thymeleaf frontend for UI rendering
        model.addAttribute("loginError", true);
        model.addAttribute("attemptedEmail", email);
        model.addAttribute("error", "Invalid Email or Password");

        return "login";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        // Terminate the active HTTP session layer and clear all stored attributes
        if (session != null) {
            session.invalidate();
        }
        // Redirect back to the login screen with a clean slate
        return "redirect:/?logout=true";
    }

    // Display the registration form ONLY to an authenticated admin
    @GetMapping("/admin/register")
    public String showRegisterPage(HttpSession session) {
        // Safety check: verify the current session is an admin
        String userRole = (String) session.getAttribute("userRole");
        if (!"ADMIN".equalsIgnoreCase(userRole)) {
            return "redirect:/"; // Kick unauthenticated clients back to login
        }
        return "register"; // Loads your beautiful register.html template
    }

    // Process form submission
    @PostMapping("/admin/register")
    public String processRegistration(
            @RequestParam("fullname") String fullname,
            @RequestParam("email") String email,
            @RequestParam("password") String password,
            HttpSession session) {

        // Safety check: verify session authority
        String userRole = (String) session.getAttribute("userRole");
        if (!"ADMIN".equalsIgnoreCase(userRole)) {
            return "redirect:/";
        }

        // New accounts saved here will trigger your database's 'trg_hash_password'
        // automatically, protecting the user's plain text input!
        User newUser = new User();
        newUser.setFullname(fullname);
        newUser.setEmail(email);
        newUser.setPassword(password); // Sent raw; PostgreSQL intercepts and hashes it
        newUser.setRole("USER"); // Default fallback profile tier

        userRepository.save(newUser);


        return "redirect:/admin?registrationSuccess=true"; // Redirect back to admin console area
    }
}