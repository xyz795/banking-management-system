package com.ashish.banking.repository;

import com.ashish.banking.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TransactionRepository
                extends JpaRepository<Transaction, Long> {

        List<Transaction> findByAccountNumberOrderByTransactionDateDesc(
                        String accountNumber);
}