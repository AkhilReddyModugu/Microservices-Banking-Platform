package com.example.accountservices.controller;

import com.example.accountservices.dto.AccountDTO;
import com.example.accountservices.dto.BankDto;
import com.example.accountservices.service.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/accounts")
public class AccountController {

    @Autowired
    private AccountService accountService;

    //all accounts
    @GetMapping
    public ResponseEntity<List<AccountDTO>> getAllAccounts() {
        return ResponseEntity.ok(accountService.getAllAccounts());
    }

    //account by id
    @GetMapping("/{id}")
    public ResponseEntity<AccountDTO> getAccountById(@PathVariable Long id) {
        AccountDTO accountDTO = accountService.getAccountById(id);
        return accountDTO != null ? ResponseEntity.ok(accountDTO) : ResponseEntity.notFound().build();
    }

    //for transaction only
    @GetMapping("customer/{id}")
    public ResponseEntity<AccountDTO> getAccountByCustomerId(@PathVariable Long id) {
        return ResponseEntity.ok(accountService.getAccountByCustomerId(id));
    }

    //account by account number
    @GetMapping("account/{accountNumber}")
    public ResponseEntity<AccountDTO> getAccountByAccountNumber(@PathVariable String accountNumber) {
        return ResponseEntity.ok(accountService.getAccountByAccountNumber(accountNumber));
    }

    //account creation
    @PostMapping
    public BankDto createAccount(@RequestBody AccountDTO accountDTO) {
        return accountService.createAccount(accountDTO);
    }

    @PutMapping("/update")
    public String saveAccount(@RequestBody AccountDTO accountDTO) {
        return accountService.saveAccount(accountDTO.getAccountNumber(), accountDTO.getBalance());
    }

    //account deletion
    @DeleteMapping("/{accountNumber}")
    public BankDto deleteAccount(@PathVariable String accountNumber) {
        return accountService.deleteAccount(accountNumber);
    }
}
