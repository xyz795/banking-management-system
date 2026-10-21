package com.ashish.banking.controller;

import com.ashish.banking.entity.Customer;
import com.ashish.banking.service.CustomerService;

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

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
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
            Model model,
            RedirectAttributes redirectAttributes) {

        try {
            Customer customer = customerService.createAccount(
                    fullName, mobileNumber, initialDeposit);

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
            Model model) {

        try {
            Customer customer = customerService.getAccountDetails(accountNumber);

            model.addAttribute("customer", customer);

        } catch (IllegalArgumentException ex) {
            model.addAttribute("errorMessage", ex.getMessage());
        }

        return "check-balance";
    }
}
