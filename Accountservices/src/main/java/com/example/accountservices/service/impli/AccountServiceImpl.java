package com.example.accountservices.service.impli;

import com.example.accountservices.dto.AccountDTO;
import com.example.accountservices.dto.BankDto;

import java.math.BigDecimal;
import java.util.List;

public interface AccountServiceImpl {

    List<AccountDTO> getAllAccounts();
    AccountDTO getAccountById(Long id);
    AccountDTO getAccountByCustomerId(Long customerId);
    AccountDTO getAccountByAccountNumber(String accountNumber);
    BankDto createAccount(AccountDTO accountDTO);
    BankDto deleteAccount(String accountNumber);
    String saveAccount(String accountNumber, BigDecimal balance);
}
