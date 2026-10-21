package com.ashish.banking.controller;

import com.ashish.banking.entity.Customer;
import com.ashish.banking.repository.CustomerRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Controller
public class AdminController {

    private final CustomerRepository customerRepository;

    public AdminController(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    // Admin Dashboard and Customer Search
    @GetMapping("/admin/dashboard")
    public String adminDashboard(
            @RequestParam(required = false) String accountNumber,
            Model model) {

        List<Customer> customers;

        boolean searched = accountNumber != null
                && !accountNumber.trim().isEmpty();

        if (searched) {

            Optional<Customer> customer = customerRepository.findByAccountNumber(
                    accountNumber.trim());

            customers = customer.map(List::of).orElseGet(List::of);

            model.addAttribute(
                    "searchAccountNumber",
                    accountNumber.trim());

            if (customers.isEmpty()) {
                model.addAttribute(
                        "searchMessage",
                        "Is account number ka customer nahi mila.");
            }

        } else {
            customers = customerRepository.findAll();
        }

        BigDecimal totalBalance = customers.stream()
                .map(Customer::getBalance)
                .filter(balance -> balance != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        model.addAttribute("totalCustomers", customers.size());
        model.addAttribute("totalBalance", totalBalance);
        model.addAttribute("customers", customers);

        return "admin-dashboard";
    }

    // View Individual Customer Details
    @GetMapping("/admin/customer-details")
    public String customerDetails(
            @RequestParam String accountNumber,
            Model model) {

        Optional<Customer> customer = customerRepository.findByAccountNumber(
                accountNumber.trim());

        if (customer.isEmpty()) {
            return "redirect:/admin/dashboard";
        }

        model.addAttribute("customer", customer.get());

        return "customer-details";
    }
}