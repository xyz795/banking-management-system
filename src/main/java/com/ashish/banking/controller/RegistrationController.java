package com.ashish.banking.controller;

import com.ashish.banking.entity.User;
import com.ashish.banking.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class RegistrationController {

    private final UserService userService;

    public RegistrationController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/register")
    public String showRegistrationPage() {
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(
            @RequestParam String fullName,
            @RequestParam String email,
            @RequestParam String password,
            Model model) {

        try {
            User user = userService.registerUser(
                    fullName.trim(),
                    email.trim().toLowerCase(),
                    password);

            return "redirect:/login?registered";

        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return "register";
        }
    }
}