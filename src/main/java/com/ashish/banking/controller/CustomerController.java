package com.ashish.banking.controller;

import com.ashish.banking.entity.Customer;
import com.ashish.banking.entity.User;
import com.ashish.banking.service.CustomerService;
import com.ashish.banking.service.UserService;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;

@Controller
public class CustomerController {

    private final CustomerService customerService;
    private final UserService userService;

    public CustomerController(
            CustomerService customerService,
            UserService userService) {
        this.customerService = customerService;
        this.userService = userService;
    }

    @GetMapping("/create-account")
    public String showCreateAccountForm() {
        return "create-account";
    }

    @PostMapping("/create-account")
    public String createAccount(
            @RequestParam String fullName,
            @RequestParam String mobileNumber,
            @RequestParam BigDecimal initialDeposit,
            Authentication authentication,
            Model model,
            RedirectAttributes redirectAttributes) {

        try {
            if (authentication == null
                    || !authentication.isAuthenticated()) {
                return "redirect:/login";
            }

            User owner = userService.findByEmail(
                    authentication.getName())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Logged-in user was not found."));

            Customer customer = customerService.createAccountForUser(
                    fullName,
                    mobileNumber,
                    initialDeposit,
                    owner);

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Account created successfully! Account Number: "
                            + customer.getAccountNumber());

            return "redirect:/create-account";

        } catch (IllegalArgumentException ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            return "create-account";
        }
    }

    @GetMapping("/check-balance")
    public String showBalanceForm() {
        return "check-balance";
    }

    @PostMapping("/check-balance")
    public String checkBalance(
            @RequestParam String accountNumber,
            Authentication authentication,
            Model model) {

        try {
            if (authentication == null
                    || !authentication.isAuthenticated()) {
                return "redirect:/login";
            }

            Customer customer = customerService.getAccountDetailsForUser(
                    accountNumber,
                    authentication.getName());

            model.addAttribute("customer", customer);

        } catch (IllegalArgumentException ex) {
            model.addAttribute("errorMessage", ex.getMessage());
        }

        return "check-balance";
    }
}