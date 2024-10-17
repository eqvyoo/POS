package com.dlnl.deliveryguard.repository;

import com.dlnl.deliveryguard.domain.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long>, QuerydslPredicateExecutor<Customer>, CustomCustomerRepository {
    Optional<Customer> findByCustomerID(String customerID);

    Optional<Customer> findByPhoneNumber(String customerPhoneNumber);
}