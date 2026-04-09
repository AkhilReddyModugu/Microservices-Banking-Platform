package com.example.transactionservice.feignconfig;


import com.example.transactionservice.dto.AccountDTO;
import com.example.transactionservice.dto.BankDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient("ACCOUNT-SERVICE")
public interface AccountServiceClient {

    @GetMapping("/accounts/customer/{id}")
    AccountDTO getAccountByCustomerId(@PathVariable Long id);

    @GetMapping("/accounts/account/{accountNumber}")
    AccountDTO getAccountByAccountNumber(@PathVariable String accountNumber);

    @DeleteMapping("/accounts/{accountNumber}")
    BankDto deleteAccount(@PathVariable String accountNumber);

    @PutMapping("/accounts/update")
    void saveAccount(@RequestBody AccountDTO accountDTO);
}