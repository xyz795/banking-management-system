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
            Model model,
            RedirectAttributes redirectAttributes) {

        try {
            Customer customer = customerService.withdrawMoney(accountNumber, amount);

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Withdrawal successful! Remaining balance: ₹"
                            + customer.getBalance());

            return "redirect:/withdraw";

        } catch (IllegalArgumentException ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            return "withdraw";
        }
    }
}
