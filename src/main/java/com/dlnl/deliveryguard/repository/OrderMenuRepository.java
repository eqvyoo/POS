package com.dlnl.deliveryguard.repository;

import com.dlnl.deliveryguard.domain.OrderMenu;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderMenuRepository extends JpaRepository<OrderMenu, Long> {
}
