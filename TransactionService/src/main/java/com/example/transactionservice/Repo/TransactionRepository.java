package com.example.transactionservice.Repo;

import com.example.transactionservice.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findTransactionsByTransactionType(String transactionType);
    List<Transaction> findByAccountNumber(String accountNumber);
}
