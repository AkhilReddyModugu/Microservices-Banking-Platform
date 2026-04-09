package com.example.transactionservice.controller;

import com.example.transactionservice.dto.BankDto;
import com.example.transactionservice.dto.TransferDTO;
import com.example.transactionservice.service.TransactionServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.example.transactionservice.dto.TransactionDTO;

import java.util.List;

@RestController
@RequestMapping("/transactions")
public class TransactionController {

    @Autowired
    private TransactionServiceImpl transactionService;

    //all transaction
    @GetMapping
    public ResponseEntity<List<TransactionDTO>> getAllTransactions() {
        return ResponseEntity.ok(transactionService.getAllTransactions());
    }

    //transaction by account number
    @GetMapping("/account/{accountNumber}")
    public ResponseEntity<List<TransactionDTO>> getTransactionsByAccountNumber(@PathVariable String accountNumber) {
        return ResponseEntity.ok(transactionService.getTransactionsByAccountNumber(accountNumber));
    }

    //transaction by account type
    @GetMapping("/transaction/{transactionType}")
    public ResponseEntity<List<TransactionDTO>> getTransactionsByTransactionType(@PathVariable String transactionType) {
        return ResponseEntity.ok(transactionService.getTransactionsByTransactionType(transactionType));
    }

    //debit transaction
    @PostMapping("/debit-transfer")
    public BankDto debitTransfer(@RequestBody TransactionDTO transactionDTO) {
        return transactionService.debitTransaction(transactionDTO);
    }

    //credit transaction
    @PostMapping("/credit-transfer")
    public BankDto creditTransfer(@RequestBody TransactionDTO transactionDTO) {
        return transactionService.creditTransaction(transactionDTO);
    }

    //transaction between users
    @PostMapping("/transfer")
    public BankDto transfer(@RequestBody TransferDTO transferDTO) {
        return transactionService.transferBetweenUsers(transferDTO);
    }
}
