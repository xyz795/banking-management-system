package com.ashish.banking.controller;

import com.ashish.banking.entity.Customer;
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
public class WithdrawController {

    private final CustomerService customerService;

    public WithdrawController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @GetMapping("/withdraw")
    public String showWithdrawForm() {
        return "withdraw";
    }

    @PostMapping("/withdraw")
    public String withdrawMoney(
            @RequestParam String accountNumber,
            @RequestParam BigDecimal amount,
            Authentication authentication,
            Model model,
            RedirectAttributes redirectAttributes) {

        try {
            if (authentication == null
                    || !authentication.isAuthenticated()) {
                return "redirect:/login";
            }

            Customer updatedCustomer = customerService.withdrawMoneyForUser(
                    accountNumber,
                    amount,
                    authentication.getName());

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Withdrawal successful! Remaining balance: ₹"
                            + updatedCustomer.getBalance());

            return "redirect:/withdraw";

        } catch (IllegalArgumentException ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            model.addAttribute("accountNumber", accountNumber);
            model.addAttribute("amount", amount);
            return "withdraw";
        }
    }
}