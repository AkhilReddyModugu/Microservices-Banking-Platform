package com.example.transactionservice.feignconfig;

import com.example.transactionservice.dto.CustomerDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient("CUSTOMER-SERVICE")
public interface CustomerServiceClient {

    @GetMapping("/customers/{id}")
    CustomerDTO getCustomerById(@PathVariable Long id);
}
