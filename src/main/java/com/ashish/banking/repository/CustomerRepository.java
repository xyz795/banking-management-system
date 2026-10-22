package com.ashish.banking.repository;

import com.ashish.banking.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, Long> {

    boolean existsByMobileNumber(String mobileNumber);

    Optional<Customer> findByAccountNumber(String accountNumber);

    Optional<Customer> findByAccountNumberAndOwner_Email(
            String accountNumber, String email);

    List<Customer> findByOwner_Email(String email);
}