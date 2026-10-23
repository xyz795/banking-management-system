package com.ashish.banking.repository;

import com.ashish.banking.entity.Customer;

import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, Long> {

    // Check whether a mobile number is already registered
    boolean existsByMobileNumber(String mobileNumber);

    // Find account by account number
    Optional<Customer> findByAccountNumber(String accountNumber);

    // Find an account belonging to a particular user
    Optional<Customer> findByAccountNumberAndOwner_Email(
            String accountNumber,
            String email);

    // Find all accounts belonging to a particular user
    List<Customer> findByOwner_Email(String email);

    // Lock an account while performing a transaction
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM Customer c WHERE c.accountNumber = :accountNumber")
    Optional<Customer> findByAccountNumberForUpdate(
            @Param("accountNumber") String accountNumber);

    // Lock an account only if it belongs to the logged-in user
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT c FROM Customer c
            WHERE c.accountNumber = :accountNumber
            AND c.owner.email = :email
            """)
    Optional<Customer> findByAccountNumberAndOwnerEmailForUpdate(
            @Param("accountNumber") String accountNumber,
            @Param("email") String email);
}