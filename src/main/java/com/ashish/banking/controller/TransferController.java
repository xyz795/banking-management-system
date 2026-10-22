package com.ashish.banking.controller;

import com.ashish.banking.service.CustomerService;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;

@Controller
public class TransferController {

    private final CustomerService customerService;

    public TransferController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @GetMapping("/transfer")
    public String showTransferForm() {
        return "transfer";
    }

    @PostMapping("/transfer")
    public String transferMoney(
            @RequestParam String senderAccountNumber,
            @RequestParam String receiverAccountNumber,
            @RequestParam BigDecimal amount,
            Authentication authentication,
            Model model,
            RedirectAttributes redirectAttributes) {

        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                return "redirect:/login";
            }

            customerService.transferMoneyForUser(
                    senderAccountNumber,
                    receiverAccountNumber,
                    amount,
                    authentication.getName());

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Transfer successful!");

            return "redirect:/transfer";

        } catch (IllegalArgumentException ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            model.addAttribute("senderAccountNumber", senderAccountNumber);
            model.addAttribute("receiverAccountNumber", receiverAccountNumber);
            model.addAttribute("amount", amount);
            return "transfer";
        }
    }
}