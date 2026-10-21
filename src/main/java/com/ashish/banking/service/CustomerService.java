package com.ashish.banking.service;

import com.ashish.banking.entity.Customer;
import com.ashish.banking.entity.Transaction;
import com.ashish.banking.repository.CustomerRepository;
import com.ashish.banking.repository.TransactionRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final TransactionRepository transactionRepository;

    public CustomerService(
            CustomerRepository customerRepository,
            TransactionRepository transactionRepository) {

        this.customerRepository = customerRepository;
        this.transactionRepository = transactionRepository;
    }

    // 1. Create New Bank Account
    @Transactional
    public Customer createAccount(
            String fullName,
            String mobileNumber,
            BigDecimal initialDeposit) {

        if (fullName == null || fullName.isBlank()) {
            throw new IllegalArgumentException("Name is required.");
        }

        if (mobileNumber == null ||
                !mobileNumber.matches("[0-9]{10}")) {
            throw new IllegalArgumentException(
                    "Enter a valid 10-digit mobile number.");
        }

        if (customerRepository.existsByMobileNumber(mobileNumber)) {
            throw new IllegalArgumentException(
                    "This mobile number is already registered.");
        }

        if (initialDeposit == null ||
                initialDeposit.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(
                    "Initial deposit cannot be negative.");
        }

        Customer customer = new Customer();

        customer.setFullName(fullName.trim());
        customer.setMobileNumber(mobileNumber);

        String accountNumber;

        do {
            accountNumber = String.valueOf(
                    1000000000L +
                            (long) (Math.random() * 9000000000L));
        } while (customerRepository
                .findByAccountNumber(accountNumber).isPresent());

        customer.setAccountNumber(accountNumber);
        customer.setBalance(initialDeposit);

        Customer savedCustomer = customerRepository.save(customer);

        // Initial deposit ko transaction history mein save karo
        if (initialDeposit.compareTo(BigDecimal.ZERO) > 0) {
            saveTransaction(
                    savedCustomer.getAccountNumber(),
                    "DEPOSIT",
                    initialDeposit);
        }

        return savedCustomer;
    }

    // 2. Check Account Details
    public Customer getAccountDetails(String accountNumber) {

        if (accountNumber == null || accountNumber.isBlank()) {
            throw new IllegalArgumentException(
                    "Account number is required.");
        }

        return customerRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new IllegalArgumentException("Account not found!"));
    }

    // 3. Deposit Money
    @Transactional
    public Customer depositMoney(
            String accountNumber,
            BigDecimal amount) {

        validateAccountNumber(accountNumber);
        validateAmount(amount);

        Customer customer = getAccountDetails(accountNumber);

        BigDecimal currentBalance = customer.getBalance();

        if (currentBalance == null) {
            currentBalance = BigDecimal.ZERO;
        }

        customer.setBalance(currentBalance.add(amount));

        Customer updatedCustomer = customerRepository.save(customer);

        saveTransaction(
                updatedCustomer.getAccountNumber(),
                "DEPOSIT",
                amount);

        return updatedCustomer;
    }

    // 4. Withdraw Money
    @Transactional
    public Customer withdrawMoney(
            String accountNumber,
            BigDecimal amount) {

        validateAccountNumber(accountNumber);
        validateAmount(amount);

        Customer customer = getAccountDetails(accountNumber);

        BigDecimal currentBalance = customer.getBalance();

        if (currentBalance == null) {
            currentBalance = BigDecimal.ZERO;
        }

        if (currentBalance.compareTo(amount) < 0) {
            throw new IllegalArgumentException(
                    "Insufficient balance!");
        }

        customer.setBalance(currentBalance.subtract(amount));

        Customer updatedCustomer = customerRepository.save(customer);

        saveTransaction(
                updatedCustomer.getAccountNumber(),
                "WITHDRAW",
                amount);

        return updatedCustomer;
    }

    // 5. Save Transaction in Database
    private void saveTransaction(
            String accountNumber,
            String type,
            BigDecimal amount) {

        Transaction transaction = new Transaction();

        transaction.setAccountNumber(accountNumber);
        transaction.setType(type);
        transaction.setAmount(amount);
        transaction.setTransactionDate(LocalDateTime.now());

        transactionRepository.save(transaction);
    }

    // 6. Validate Account Number
    private void validateAccountNumber(String accountNumber) {

        if (accountNumber == null || accountNumber.isBlank()) {
            throw new IllegalArgumentException(
                    "Account number is required.");
        }
    }

    // 7. Validate Amount
    private void validateAmount(BigDecimal amount) {

        if (amount == null ||
                amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "Amount must be greater than zero.");
        }
    }
}