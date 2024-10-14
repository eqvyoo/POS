package com.dlnl.deliveryguard.repository;

import com.dlnl.deliveryguard.domain.Store;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StoreRepository extends JpaRepository<Store, Long> {
}