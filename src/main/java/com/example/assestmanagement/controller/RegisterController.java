package com.example.assestmanagement.controller;

import com.example.assestmanagement.model.User;

import com.example.assestmanagement.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
public class RegisterController {

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/register")
    public String registerPage() {
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(User user) {

        user.setRole("USER");

        userRepository.save(user);

        return "redirect:/";
    }
}