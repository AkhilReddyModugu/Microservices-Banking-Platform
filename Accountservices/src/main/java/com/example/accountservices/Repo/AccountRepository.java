package com.example.accountservices.Repo;

import com.example.accountservices.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;


public interface AccountRepository extends JpaRepository<Account, Long> {
    Account findAccountByCustomerId(long customerId);
    Boolean existsAccountByCustomerId(long customerId);
    Boolean existsAccountByAccountNumber(String accountNumber);
    void deleteAccountByAccountNumber(String accountNumber);
    Account findAccountByAccountNumber(String accountNumber);
}
