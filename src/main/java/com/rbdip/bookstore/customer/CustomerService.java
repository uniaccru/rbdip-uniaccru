package com.rbdip.bookstore.customer;

import org.springframework.stereotype.Service;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;

    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    public Customer findOrCreate(String fullName, String address, String phone) {
        String normalizedName = fullName.trim();
        int separator = normalizedName.indexOf(' ');
        String firstName = separator < 0 ? normalizedName : normalizedName.substring(0, separator);
        String lastName = separator < 0 ? "" : normalizedName.substring(separator + 1).trim();
        return customerRepository.findMatching(firstName, lastName, address, phone)
                .orElseGet(() -> customerRepository.save(new Customer(firstName, lastName, address, phone)));
    }
}
