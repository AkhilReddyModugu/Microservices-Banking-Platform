package com.example.coustomerservices.Repo;

import com.example.coustomerservices.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
    Boolean existsByEmail(String email);
    Customer findCustomerById(Long id);
    void deleteCustomerById(Long customerId);

}