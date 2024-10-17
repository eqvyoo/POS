package com.dlnl.deliveryguard.repository;

import com.dlnl.deliveryguard.domain.Menu;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MenuRepository extends JpaRepository<Menu, Long> {
    Optional<Menu> findByName(String menuName);
}
