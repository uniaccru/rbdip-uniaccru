package com.rbdip.bookstore.customer;

import org.springframework.stereotype.Service;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;

    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    public Customer findOrCreate(String fullName, String address, String phone) {
        return customerRepository.findMatching(fullName, address, phone)
                .orElseGet(() -> customerRepository.save(new Customer(fullName, address, phone)));
    }
}
