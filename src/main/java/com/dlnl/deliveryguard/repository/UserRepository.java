package com.dlnl.deliveryguard.repository;

import com.dlnl.deliveryguard.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findById(Long id);
    Optional<User> findByLoginID(String loginID);

    Optional<User> findByEmail(String email);
    boolean existsByLoginID(String loginID);
    boolean existsByEmail(String email);

}