package com.dlnl.deliveryguard.repository;

import com.dlnl.deliveryguard.domain.SmsSendCondition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SmsSendConditionRepository extends JpaRepository<SmsSendCondition, Long> {
    List<SmsSendCondition> findByUserId(Long userId);
}