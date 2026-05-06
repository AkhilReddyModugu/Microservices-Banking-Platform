package com.example.customerservices.service.impli;

import com.example.customerservices.dto.BankDto;
import com.example.customerservices.dto.CustomerDTO;

import java.util.List;

public interface CustomerImpl {
    List<CustomerDTO> getAllCustomers();
    BankDto createAccount(CustomerDTO customerDTO);
    CustomerDTO getCustomerById(Long id);
    BankDto deleteCustomer(Long id);
}
