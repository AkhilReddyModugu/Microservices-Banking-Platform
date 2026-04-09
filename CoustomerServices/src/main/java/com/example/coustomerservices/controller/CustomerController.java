package com.example.coustomerservices.controller;

import com.example.coustomerservices.dto.BankDto;
import com.example.coustomerservices.dto.CustomerDTO;
import com.example.coustomerservices.service.CustomerServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@RestController
@RequestMapping("/customers")
public class CustomerController {

    @Autowired
    private CustomerServiceImpl customerService;

    //all customers
    @GetMapping
    public ResponseEntity<List<CustomerDTO>> getAllCustomers() {
        return ResponseEntity.ok(customerService.getAllCustomers());
    }

    //customer by id
    @GetMapping("/{id}")
    public ResponseEntity<CustomerDTO> getCustomerById(@PathVariable Long id) {
        CustomerDTO customerDTO = customerService.getCustomerById(id);
        return customerDTO != null ? ResponseEntity.ok(customerDTO) : ResponseEntity.notFound().build();
    }

    //customer creation
    @PostMapping
    public BankDto createCustomer(@RequestBody CustomerDTO customerDTO) {
        return customerService.createAccount(customerDTO);
    }

    //customer deletion
    @DeleteMapping("/{id}")
    public BankDto deleteCustomer(@PathVariable Long id) {
        return customerService.deleteCustomer(id);
    }
}