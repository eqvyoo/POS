package com.dlnl.deliveryguard.repository;

import com.dlnl.deliveryguard.domain.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    Optional<Order> findByOrderNumberAndOrderPlatform(String orderNumber, String orderPlatform);
}