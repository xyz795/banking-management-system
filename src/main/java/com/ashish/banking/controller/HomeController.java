package com.ashish.banking.controller;

import com.ashish.banking.entity.Customer;
import com.ashish.banking.repository.CustomerRepository;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.math.BigDecimal;
import java.util.List;

@Controller
public class HomeController {

    private final CustomerRepository customerRepository;

    public HomeController(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @GetMapping("/")
    public String home(Model model) {

        List<Customer> customers = customerRepository.findAll();

        BigDecimal totalBalance = customers.stream()
                .map(Customer::getBalance)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        model.addAttribute("totalCustomers", customers.size());
        model.addAttribute("totalBalance", totalBalance);

        return "dashboard";
    }
}