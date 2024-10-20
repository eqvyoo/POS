package com.dlnl.deliveryguard.repository;

import com.dlnl.deliveryguard.domain.Order;
import com.dlnl.deliveryguard.domain.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> , CustomOrderRepository{
    Optional<Order> findByOrderNumberAndOrderPlatform(String orderNumber, String orderPlatform);
    List<Order> findAllByStatus(Status status);

}