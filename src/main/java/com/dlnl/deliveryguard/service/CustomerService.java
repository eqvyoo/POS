package com.dlnl.deliveryguard.service;

import com.dlnl.deliveryguard.domain.Customer;
import com.dlnl.deliveryguard.repository.CustomerRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;

    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @Transactional(readOnly = true)
    public Page<Customer> getCustomers(String nickname, String phoneNumber, String address, Long storeId, Pageable pageable) {
        return customerRepository.searchCustomers(nickname, phoneNumber, address, storeId, pageable);
    }
}