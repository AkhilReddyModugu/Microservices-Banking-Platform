package com.example.transactionservice.service.impli;

import com.example.transactionservice.dto.BankDto;
import com.example.transactionservice.dto.TransactionDTO;
import com.example.transactionservice.dto.TransferDTO;

import java.util.List;

public interface TransactionService {

    List<TransactionDTO> getAllTransactions();
    List<TransactionDTO> getTransactionsByAccountNumber(String accountNumber);
    List<TransactionDTO> getTransactionsByTransactionType(String transactionType);
    BankDto creditTransaction(TransactionDTO transactionDTO);
    BankDto debitTransaction(TransactionDTO transactionDTO);
    BankDto transferBetweenUsers(TransferDTO transferDTO);
}
