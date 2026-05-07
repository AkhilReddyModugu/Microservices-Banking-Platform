package com.example.transactionservice.service;

import com.example.transactionservice.Repo.TransactionRepository;
import com.example.transactionservice.dto.*;
import com.example.transactionservice.entity.Transaction;
import com.example.transactionservice.feignconfig.AccountServiceClient;
import com.example.transactionservice.feignconfig.CustomerServiceClient;
import com.example.transactionservice.service.impli.TransactionService;
import com.example.transactionservice.transactionUtils.TransactionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TransactionServiceImpl implements TransactionService {

    private static final Logger log = LoggerFactory.getLogger(TransactionServiceImpl.class);

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private AccountServiceClient accountServiceClient;

    @Autowired
    private CustomerServiceClient customerServiceClient;

    @Value("${account.exchange.name}")
    private String EXCHANGE_NAME;

    @Value("${account.routing.json.key}")
    private String JSON_ROUTING_KEY;

    // Get all transactions
    public List<TransactionDTO> getAllTransactions() {
        return transactionRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // Get transactions by account number
    public List<TransactionDTO> getTransactionsByAccountNumber(String accountNumber) {
        return transactionRepository.findByAccountNumber(accountNumber).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // Get transactions by type
    public List<TransactionDTO> getTransactionsByTransactionType(String transactionType) {
        return transactionRepository.findTransactionsByTransactionType(transactionType).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // Credit transaction
    public BankDto creditTransaction(TransactionDTO transactionDTO) {
        AccountDTO account = accountServiceClient.getAccountByAccountNumber(transactionDTO.getAccountNumber());
        if (account == null) {
            log.warn("Credit failed — account not found: {}", transactionDTO.getAccountNumber());
            return BankDto.builder()
                    .responseCode(TransactionUtils.ACCOUNT_NOT_EXISTS_CODE)
                    .responseMessage(TransactionUtils.ACCOUNT_NOT_EXISTS_MESSAGE)
                    .build();
        }
        account.setBalance(account.getBalance().add(transactionDTO.getAmount()));
        processTransaction(account, "CREDIT", transactionDTO.getAmount(), "credited by the user");
        log.info("Credit completed: accountNumber={}, amount={}", transactionDTO.getAccountNumber(), transactionDTO.getAmount());
        return buildResponse(account, transactionDTO.getAmount(),
                TransactionUtils.CREDIT_TRANSACTION_COMPLETED_CODE,
                TransactionUtils.CREDIT_TRANSACTION_COMPLETED_MESSAGE,
                "Credit done successfully");
    }

    // Debit transaction
    public BankDto debitTransaction(TransactionDTO transactionDTO) {
        AccountDTO account = accountServiceClient.getAccountByAccountNumber(transactionDTO.getAccountNumber());
        if (account == null) {
            log.warn("Debit failed — account not found: {}", transactionDTO.getAccountNumber());
            return BankDto.builder()
                    .responseCode(TransactionUtils.ACCOUNT_NOT_EXISTS_CODE)
                    .responseMessage(TransactionUtils.ACCOUNT_NOT_EXISTS_MESSAGE)
                    .build();
        }

        if (account.getBalance().compareTo(transactionDTO.getAmount()) >= 0) {
            account.setBalance(account.getBalance().subtract(transactionDTO.getAmount()));
            processTransaction(account, "DEBIT", transactionDTO.getAmount(), "debited by user");
            log.info("Debit completed: accountNumber={}, amount={}", transactionDTO.getAccountNumber(), transactionDTO.getAmount());
            return buildResponse(account, transactionDTO.getAmount(),
                    TransactionUtils.DEBIT_TRANSACTION_COMPLETED_CODE,
                    TransactionUtils.DEBIT_TRANSACTION_COMPLETED_MESSAGE,
                    "Withdrawal done successfully");
        }

        log.warn("Debit failed — insufficient balance: accountNumber={}, balance={}, requested={}",
                transactionDTO.getAccountNumber(), account.getBalance(), transactionDTO.getAmount());
        return buildResponse(account, transactionDTO.getAmount(),
                TransactionUtils.DEBIT_TRANSACTION_FAILED_CODE,
                TransactionUtils.DEBIT_TRANSACTION_FAILED_MESSAGE,
                "Withdrawal failed");
    }

    // Transfer between users
    public BankDto transferBetweenUsers(TransferDTO transferDTO) {
        AccountDTO senderAccount = accountServiceClient.getAccountByAccountNumber(transferDTO.getFromAccount());
        AccountDTO recipientAccount = accountServiceClient.getAccountByAccountNumber(transferDTO.getToAccount());

        if (senderAccount == null || recipientAccount == null) {
            log.warn("Transfer failed — account not found: from={}, to={}", transferDTO.getFromAccount(), transferDTO.getToAccount());
            return BankDto.builder()
                    .responseCode(TransactionUtils.ACCOUNT_NOT_EXISTS_CODE)
                    .responseMessage(TransactionUtils.ACCOUNT_NOT_EXISTS_MESSAGE)
                    .build();
        }

        if (senderAccount.getAccountNumber().equals(recipientAccount.getAccountNumber())) {
            log.warn("Transfer failed — self-transfer attempted: accountNumber={}", transferDTO.getFromAccount());
            return buildResponse(senderAccount, transferDTO.getAmount(),
                    TransactionUtils.SELF_TRANSACTION_CODE,
                    TransactionUtils.SELF_TRANSACTION_MESSAGE,
                    "cannot do self transfer, use different account number.");
        }
        if (transferDTO.getAmount().compareTo(BigDecimal.TEN) < 0) {
            log.warn("Transfer failed — below minimum amount: amount={}", transferDTO.getAmount());
            return buildResponse(senderAccount, transferDTO.getAmount(),
                    TransactionUtils.MINIMUM_TRANSACTION_AMOUNT_CODE,
                    TransactionUtils.MINIMUM_TRANSACTION_AMOUNT_MESSAGE,
                    "please choose the minimum amount for transaction.");
        }
        if (senderAccount.getBalance().compareTo(transferDTO.getAmount()) >= 0) {
            senderAccount.setBalance(senderAccount.getBalance().subtract(transferDTO.getAmount()));
            recipientAccount.setBalance(recipientAccount.getBalance().add(transferDTO.getAmount()));

            processTransaction(senderAccount, "DEBIT", transferDTO.getAmount(), "sent to " + recipientAccount.getAccountNumber());
            processTransaction(recipientAccount, "CREDIT", transferDTO.getAmount(), "received from " + senderAccount.getAccountNumber());

            log.info("Transfer completed: from={}, to={}, amount={}", transferDTO.getFromAccount(), transferDTO.getToAccount(), transferDTO.getAmount());
            return buildResponse(senderAccount, transferDTO.getAmount(),
                    TransactionUtils.TRANSACTION_COMPLETED_CODE,
                    TransactionUtils.TRANSACTION_COMPLETED_MESSAGE,
                    "Amount transferred to receiver");
        }

        log.warn("Transfer failed — insufficient balance: from={}, balance={}, requested={}",
                transferDTO.getFromAccount(), senderAccount.getBalance(), transferDTO.getAmount());
        return buildResponse(senderAccount, senderAccount.getBalance(),
                TransactionUtils.LOW_BALANCE_CODE + " AND " + TransactionUtils.TRANSACTION_FAILED_CODE,
                TransactionUtils.LOW_BALANCE_MESSAGE + ", " + TransactionUtils.TRANSACTION_FAILED_MESSAGE,
                "Amount not transferred to receiver");
    }

    // Helper Methods
    private void processTransaction(AccountDTO account, String type, BigDecimal amount, String message) {
        // Save transaction in the repository
        transactionRepository.save(Transaction.builder()
                .accountNumber(account.getAccountNumber())
                .amount(amount)
                .message("Amount of " + amount + " has been " + message)
                .transactionType(type)
                .build());

        // Update the account with new balance
        accountServiceClient.saveAccount(account);

        // Fetch customer details
        CustomerDTO customer = customerServiceClient.getCustomerById(account.getCustomerId());

        // send notification
        rabbitTemplate.convertAndSend(EXCHANGE_NAME, JSON_ROUTING_KEY, NotificationDTO.builder()
                .receiver(customer.getEmail())
                .subject(type.equals("CREDIT") ? "CREDIT TRANSACTION ALERT" : "DEBIT TRANSACTION ALERT")
                .body(buildDebitCreditMessage(account, amount, message))
                .build()
        );
    }

    private String buildDebitCreditMessage(AccountDTO accountDTO, BigDecimal amount, String message) {
        return "Amount of " + amount + " has been " + message + "\n\n" +
                "Account number: " + accountDTO.getAccountNumber() + "\nBalance: " + accountDTO.getBalance();
    }

    private BankDto buildResponse(AccountDTO account, BigDecimal amount, String responseCode, String responseMessage, String message) {
        return BankDto.builder()
                .responseCode(responseCode)
                .responseMessage(responseMessage)
                .accountInfo(AccountInfo.builder()
                        .accountNumber(account.getAccountNumber())
                        .amount(amount)
                        .message(message)
                        .build())
                .build();
    }

    private TransactionDTO convertToDTO(Transaction transaction) {
        return TransactionDTO.builder()
                .id(transaction.getId())
                .accountNumber(transaction.getAccountNumber())
                .amount(transaction.getAmount())
                .transactionType(transaction.getTransactionType())
                .build();
    }
}
