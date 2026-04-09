package com.example.coustomerservices.feignconfig;

import com.example.coustomerservices.dto.AccountDTO;
import com.example.coustomerservices.dto.BankDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient("ACCOUNT-SERVICE")
public interface AccountServiceClient {

    @GetMapping("/accounts/customer/{id}")
    AccountDTO getAccountByCustomerId(@PathVariable Long id);

    @DeleteMapping("/accounts/{accountNumber}")
    BankDto deleteAccount(@PathVariable String accountNumber);
}