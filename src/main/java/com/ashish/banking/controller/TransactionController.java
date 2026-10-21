package com.ashish.banking.controller;

import com.ashish.banking.entity.Transaction;
import com.ashish.banking.repository.TransactionRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class TransactionController {

    private final TransactionRepository transactionRepository;

    public TransactionController(
            TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    @GetMapping("/transactions")
    public String showTransactions(
            @RequestParam(required = false) String accountNumber,
            Model model) {

        List<Transaction> transactions = List.of();

        if (accountNumber != null && !accountNumber.isBlank()) {
            transactions = transactionRepository
                    .findByAccountNumberOrderByTransactionDateDesc(
                            accountNumber.trim());
        }

        model.addAttribute("transactions", transactions);
        model.addAttribute(
                "accountNumber",
                accountNumber == null ? "" : accountNumber.trim());
        System.out.println("Transaction History controller called");
        return "transaction";
    }
}