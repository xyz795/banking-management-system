package com.ashish.banking.service;

import com.ashish.banking.entity.Customer;
import com.ashish.banking.entity.Transaction;
import com.ashish.banking.entity.User;
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

    @Transactional
    public Customer createAccount(
            String fullName,
            String mobileNumber,
            BigDecimal initialDeposit) {

        validateAccountCreation(fullName, mobileNumber, initialDeposit);

        Customer customer = new Customer();
        customer.setFullName(fullName.trim());
        customer.setMobileNumber(mobileNumber.trim());
        customer.setAccountNumber(generateAccountNumber());
        customer.setBalance(initialDeposit);

        Customer savedCustomer = customerRepository.save(customer);

        if (initialDeposit.compareTo(BigDecimal.ZERO) > 0) {
            saveTransaction(
                    savedCustomer.getAccountNumber(),
                    "DEPOSIT",
                    initialDeposit);
        }

        return savedCustomer;
    }

    @Transactional
    public Customer createAccountForUser(
            String fullName,
            String mobileNumber,
            BigDecimal initialDeposit,
            User owner) {

        if (owner == null || owner.getId() == null) {
            throw new IllegalArgumentException(
                    "A valid account owner is required.");
        }

        validateAccountCreation(fullName, mobileNumber, initialDeposit);

        Customer customer = new Customer();
        customer.setFullName(fullName.trim());
        customer.setMobileNumber(mobileNumber.trim());
        customer.setAccountNumber(generateAccountNumber());
        customer.setBalance(initialDeposit);
        customer.setOwner(owner);

        Customer savedCustomer = customerRepository.save(customer);

        if (initialDeposit.compareTo(BigDecimal.ZERO) > 0) {
            saveTransaction(
                    savedCustomer.getAccountNumber(),
                    "DEPOSIT",
                    initialDeposit);
        }

        return savedCustomer;
    }

    private void validateAccountCreation(
            String fullName,
            String mobileNumber,
            BigDecimal initialDeposit) {

        if (fullName == null || fullName.isBlank()) {
            throw new IllegalArgumentException("Name is required.");
        }

        if (mobileNumber == null
                || !mobileNumber.trim().matches("[0-9]{10}")) {
            throw new IllegalArgumentException(
                    "Enter a valid 10-digit mobile number.");
        }

        if (customerRepository.existsByMobileNumber(mobileNumber.trim())) {
            throw new IllegalArgumentException(
                    "This mobile number is already registered.");
        }

        if (initialDeposit == null
                || initialDeposit.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(
                    "Initial deposit cannot be negative.");
        }

        validateMoneyScale(initialDeposit);
    }

    private String generateAccountNumber() {
        String accountNumber;

        do {
            accountNumber = String.valueOf(
                    1000000000L
                            + (long) (Math.random() * 9000000000L));
        } while (customerRepository
                .findByAccountNumber(accountNumber)
                .isPresent());

        return accountNumber;
    }

    public Customer getAccountDetails(String accountNumber) {
        validateAccountNumber(accountNumber);

        return customerRepository
                .findByAccountNumber(accountNumber.trim())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Account not found!"));
    }

    public Customer getAccountDetailsForUser(
            String accountNumber,
            String email) {

        validateAccountNumber(accountNumber);
        validateEmail(email);

        return customerRepository
                .findByAccountNumberAndOwner_Email(
                        accountNumber.trim(),
                        email.trim())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Account not found or you do not have permission to access it."));
    }

    @Transactional
    public Customer depositMoneyForUser(
            String accountNumber,
            BigDecimal amount,
            String email) {

        validateAmount(amount);

        Customer customer = getAccountDetailsForUser(accountNumber, email);
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

    @Transactional
    public Customer withdrawMoneyForUser(
            String accountNumber,
            BigDecimal amount,
            String email) {

        validateAmount(amount);

        Customer customer = getAccountDetailsForUser(accountNumber, email);
        BigDecimal currentBalance = customer.getBalance();

        if (currentBalance == null
                || currentBalance.compareTo(amount) < 0) {
            throw new IllegalArgumentException("Insufficient balance!");
        }

        customer.setBalance(currentBalance.subtract(amount));

        Customer updatedCustomer = customerRepository.save(customer);

        saveTransaction(
                updatedCustomer.getAccountNumber(),
                "WITHDRAW",
                amount);

        return updatedCustomer;
    }

    @Transactional
    public void transferMoneyForUser(
            String senderAccountNumber,
            String receiverAccountNumber,
            BigDecimal amount,
            String email) {

        validateAccountNumber(senderAccountNumber);
        validateAccountNumber(receiverAccountNumber);
        validateAmount(amount);
        validateEmail(email);

        String senderNumber = senderAccountNumber.trim();
        String receiverNumber = receiverAccountNumber.trim();

        if (senderNumber.equals(receiverNumber)) {
            throw new IllegalArgumentException(
                    "Sender and receiver accounts must be different.");
        }

        Customer sender = getAccountDetailsForUser(senderNumber, email);

        Customer receiver = customerRepository
                .findByAccountNumber(receiverNumber)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Receiver account not found."));

        BigDecimal senderBalance = sender.getBalance();
        BigDecimal receiverBalance = receiver.getBalance();

        if (senderBalance == null
                || senderBalance.compareTo(amount) < 0) {
            throw new IllegalArgumentException("Insufficient balance!");
        }

        if (receiverBalance == null) {
            receiverBalance = BigDecimal.ZERO;
        }

        sender.setBalance(senderBalance.subtract(amount));
        receiver.setBalance(receiverBalance.add(amount));

        customerRepository.save(sender);
        customerRepository.save(receiver);

        saveTransaction(
                sender.getAccountNumber(),
                "TRANSFER_SENT",
                amount);

        saveTransaction(
                receiver.getAccountNumber(),
                "TRANSFER_RECEIVED",
                amount);
    }

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

    private void validateAccountNumber(String accountNumber) {
        if (accountNumber == null || accountNumber.isBlank()) {
            throw new IllegalArgumentException(
                    "Account number is required.");
        }
    }

    private void validateEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException(
                    "Logged-in user is required.");
        }
    }

    private void validateAmount(BigDecimal amount) {
        if (amount == null
                || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "Amount must be greater than zero.");
        }

        validateMoneyScale(amount);
    }

    private void validateMoneyScale(BigDecimal amount) {
        if (amount.scale() > 2) {
            throw new IllegalArgumentException(
                    "Amount cannot have more than 2 decimal places.");
        }
    }
}