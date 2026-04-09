package com.example.accountservices.service;

import com.example.accountservices.Repo.AccountRepository;
import com.example.accountservices.dto.*;
import com.example.accountservices.entity.Account;
import com.example.accountservices.accountUtils.AccountUtils;
import com.example.accountservices.feignconfig.CustomerServiceClient;
import com.example.accountservices.service.impli.AccountServiceImpl;
import jakarta.transaction.Transactional;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AccountService implements AccountServiceImpl {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Value("${account.exchange.name}")
    private String EXCHANGE_NAME;

    @Value("${account.routing.json.key}")
    private String JSON_ROUTING_KEY;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private CustomerServiceClient customerServiceClient;

    // Get all accounts
    public List<AccountDTO> getAllAccounts() {
        return accountRepository.findAll().stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    // Get account by id
    public AccountDTO getAccountById(Long id) {
        return accountRepository.findById(id).map(this::convertToDTO).orElse(null);
    }

    // Get account by customer id
    public AccountDTO getAccountByCustomerId(Long customerId) {
        return accountRepository.existsAccountByCustomerId(customerId) ?
                convertToDTO(accountRepository.findAccountByCustomerId(customerId)) :
                emptyAccount();
    }

    // Get account by account number
    public AccountDTO getAccountByAccountNumber(String accountNumber) {
        return convertToDTO(accountRepository.findAccountByAccountNumber(accountNumber));
    }

    // Create account
    public BankDto createAccount(AccountDTO accountDTO) {
        if (accountRepository.existsAccountByAccountNumber(accountDTO.getAccountNumber())) {
            return buildResponse(AccountUtils.ACCOUNT_EXISTS_CODE, AccountUtils.ACCOUNT_EXISTS_MESSAGE, accountDTO.getAccountNumber(), null, null);
        }

        Account newAccount = accountRepository.save(Account.builder()
                .accountNumber(AccountUtils.generateAccountNumber())
                .accountType(accountDTO.getAccountType())
                .balance(BigDecimal.ZERO)
                .customerId(accountDTO.getCustomerId())
                .build());

        CustomerDTO customer = customerServiceClient.getCustomerById(newAccount.getCustomerId());

        rabbitTemplate.convertAndSend(EXCHANGE_NAME, JSON_ROUTING_KEY, NotificationDTO.builder()
                .receiver(customer.getEmail())
                .subject("Welcome to our Bank")
                .body(buildAccountMessage(newAccount, AccountUtils.ACCOUNT_CREATION_CODE, AccountUtils.ACCOUNT_CREATION_MESSAGE))
                .build());
        System.out.println("Notification sent to queue");
        //sending mail using synchronous communication
//        sendNotification(customer.getEmail(), "Welcome to our Bank", buildAccountMessage(newAccount, AccountUtils.ACCOUNT_CREATION_CODE, AccountUtils.ACCOUNT_CREATION_MESSAGE));

        return buildResponse(AccountUtils.ACCOUNT_CREATION_CODE, AccountUtils.ACCOUNT_CREATION_MESSAGE, newAccount.getAccountNumber(), newAccount.getBalance(), customer.getFirstName()+ " " + customer.getLastName());
    }

    // Delete account
    @Transactional
    public BankDto deleteAccount(String accountNumber) {
        if (!accountRepository.existsAccountByAccountNumber(accountNumber)) {
            return buildResponse(AccountUtils.ACCOUNT_NOT_EXISTS_CODE, AccountUtils.ACCOUNT_NOT_EXISTS_MESSAGE, null, null, null);
        }

        Account account = accountRepository.findAccountByAccountNumber(accountNumber);
        accountRepository.deleteAccountByAccountNumber(accountNumber);
        CustomerDTO customer = customerServiceClient.getCustomerById(account.getCustomerId());

        rabbitTemplate.convertAndSend(EXCHANGE_NAME, JSON_ROUTING_KEY, NotificationDTO.builder()
                .receiver(customer.getEmail())
                .subject("Account Deletion")
                .body(buildAccountMessage(account, AccountUtils.ACCOUNT_DELETION_CODE, AccountUtils.ACCOUNT_DELETION_MESSAGE))
                .build());
        System.out.println("Notification sent to queue");

        //sending mail using synchronous communication
//        sendNotification(customer.getEmail(), "Account Deletion", buildAccountMessage(account, AccountUtils.ACCOUNT_DELETION_CODE, AccountUtils.ACCOUNT_DELETION_MESSAGE));

        return buildResponse(AccountUtils.ACCOUNT_DELETION_CODE, AccountUtils.ACCOUNT_DELETION_MESSAGE, null, null, null);
    }

    // Update account balance
    public String saveAccount(String accountNumber, BigDecimal balance) {
        Account account = accountRepository.findAccountByAccountNumber(accountNumber);
        account.setBalance(balance);
        accountRepository.save(account);
        return "updated";
    }

    private String buildAccountMessage(Account account, String code, String message) {
        return "Response Code = " + code + "\n\n" +
                "Response Message = " + message + "\n\n" +
                "Account Details: Account Number: " + account.getAccountNumber() + ", Balance: " + account.getBalance();
    }

    private BankDto buildResponse(String code, String message, String accountNumber, BigDecimal balance, String accountName) {
        return BankDto.builder()
                .responseCode(code)
                .responseMessage(message)
                .accountInfo(AccountInfo.builder()
                        .accountNumber(accountNumber)
                        .accountBalance(balance)
                        .accountName(accountName)
                        .build())
                .build();
    }

    private AccountDTO convertToDTO(Account account) {
        return AccountDTO.builder()
                .accountType(account.getAccountType())
                .accountNumber(account.getAccountNumber())
                .balance(account.getBalance())
                .customerId(account.getCustomerId())
                .build();
    }

    private AccountDTO emptyAccount() {
        return AccountDTO.builder()
                .accountNumber(null)
                .balance(BigDecimal.ZERO)
                .accountType(null)
                .build();
    }
}
