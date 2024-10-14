package com.dlnl.deliveryguard.repository;

import com.dlnl.deliveryguard.domain.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CustomCustomerRepository {
    Page<Customer> searchCustomers(String nickname, String phoneNumber, String address, Long storeId, Pageable pageable);
}

