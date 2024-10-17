package com.dlnl.deliveryguard.repository;

import com.dlnl.deliveryguard.domain.SmsSendHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SmsSendHistoryRepository extends JpaRepository<SmsSendHistory, Long> {
    List<SmsSendHistory> findByCustomerContactContainingAndSendTimeBetweenAndSendStatusContaining(
            String customerContact, LocalDateTime startTime, LocalDateTime endTime, String sendStatus);
}