package com.ashish.banking.controller;

import com.ashish.banking.entity.Customer;
import com.ashish.banking.entity.Transaction;
import com.ashish.banking.repository.TransactionRepository;
import com.ashish.banking.service.CustomerService;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class TransactionController {

    private final TransactionRepository transactionRepository;
    private final CustomerService customerService;

    public TransactionController(
            TransactionRepository transactionRepository,
            CustomerService customerService) {
        this.transactionRepository = transactionRepository;
        this.customerService = customerService;
    }

    @GetMapping("/transactions")
    public String showTransactions(
            @RequestParam(required = false) String accountNumber,
            Authentication authentication,
            Model model) {

        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }

        List<Transaction> transactions = List.of();
        String searchedAccountNumber = "";

        if (accountNumber != null && !accountNumber.isBlank()) {
            try {
                Customer customer = customerService.getAccountDetailsForUser(
                        accountNumber.trim(),
                        authentication.getName());

                searchedAccountNumber = customer.getAccountNumber();

                transactions = transactionRepository
                        .findByAccountNumberOrderByTransactionDateDesc(
                                searchedAccountNumber);

            } catch (IllegalArgumentException ex) {
                model.addAttribute("errorMessage", ex.getMessage());
            }
        }

        model.addAttribute("transactions", transactions);
        model.addAttribute("accountNumber", searchedAccountNumber);

        return "transaction";
    }
}