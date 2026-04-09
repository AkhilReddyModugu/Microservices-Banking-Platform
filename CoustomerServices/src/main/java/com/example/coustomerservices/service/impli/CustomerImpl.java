package com.example.coustomerservices.service.impli;

import com.example.coustomerservices.dto.BankDto;
import com.example.coustomerservices.dto.CustomerDTO;

import java.util.List;

public interface CustomerImpl {
    List<CustomerDTO> getAllCustomers();
    BankDto createAccount(CustomerDTO customerDTO);
    CustomerDTO getCustomerById(Long id);
    BankDto deleteCustomer(Long id);
}
