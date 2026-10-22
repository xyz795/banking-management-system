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

    // 1. Create account without owner (legacy/admin use)
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

    // 2. Create account linked to the logged-in user
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

    // 3. Validate account creation information
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

    // 4. Generate a unique account number
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

    // 5. Find account by account number
    // Keep for existing administrative/internal callers.
    public Customer getAccountDetails(String accountNumber) {
        validateAccountNumber(accountNumber);

        return customerRepository
                .findByAccountNumber(accountNumber.trim())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Account not found!"));
    }

    // 6. Find account only if it belongs to the specified user
    public Customer getAccountDetailsForUser(
            String accountNumber,
            String email) {

        validateAccountNumber(accountNumber);

        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException(
                    "Logged-in user is required.");
        }

        return customerRepository
                .findByAccountNumberAndOwner_Email(
                        accountNumber.trim(),
                        email.trim())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Account not found or you do not have permission to access it."));
    }

    // 7. Existing deposit method
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

    // 8. Deposit with ownership verification
    @Transactional
    public Customer depositMoneyForUser(
            String accountNumber,
            BigDecimal amount,
            String email) {

        Customer customer = getAccountDetailsForUser(accountNumber, email);

        return depositMoney(customer.getAccountNumber(), amount);
    }

    // 9. Existing withdrawal method
    @Transactional
    public Customer withdrawMoney(
            String accountNumber,
            BigDecimal amount) {

        validateAccountNumber(accountNumber);
        validateAmount(amount);

        Customer customer = getAccountDetails(accountNumber);
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

    // 10. Withdrawal with ownership verification
    @Transactional
    public Customer withdrawMoneyForUser(
            String accountNumber,
            BigDecimal amount,
            String email) {

        Customer customer = getAccountDetailsForUser(accountNumber, email);

        return withdrawMoney(customer.getAccountNumber(), amount);
    }

    // 11. Existing transfer method
    @Transactional
    public void transferMoney(
            String senderAccountNumber,
            String receiverAccountNumber,
            BigDecimal amount) {

        validateAccountNumber(senderAccountNumber);
        validateAccountNumber(receiverAccountNumber);
        validateAmount(amount);

        String senderNumber = senderAccountNumber.trim();
        String receiverNumber = receiverAccountNumber.trim();

        if (senderNumber.equals(receiverNumber)) {
            throw new IllegalArgumentException(
                    "Sender and receiver accounts must be different.");
        }

        Customer sender = getAccountDetails(senderNumber);
        Customer receiver = getAccountDetails(receiverNumber);

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

    // 12. Transfer with sender ownership verification
    @Transactional
    public void transferMoneyForUser(
            String senderAccountNumber,
            String receiverAccountNumber,
            BigDecimal amount,
            String email) {

        Customer sender = getAccountDetailsForUser(
                senderAccountNumber,
                email);

        transferMoney(
                sender.getAccountNumber(),
                receiverAccountNumber,
                amount);
    }

    // 13. Save transaction
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

    // 14. Validate account number
    private void validateAccountNumber(String accountNumber) {
        if (accountNumber == null || accountNumber.isBlank()) {
            throw new IllegalArgumentException(
                    "Account number is required.");
        }
    }

    // 15. Validate transaction amount
    private void validateAmount(BigDecimal amount) {
        if (amount == null
                || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "Amount must be greater than zero.");
        }

        validateMoneyScale(amount);
    }

    // 16. Money must have at most two decimal places
    private void validateMoneyScale(BigDecimal amount) {
        if (amount.scale() > 2) {
            throw new IllegalArgumentException(
                    "Amount cannot have more than 2 decimal places.");
        }
    }
}