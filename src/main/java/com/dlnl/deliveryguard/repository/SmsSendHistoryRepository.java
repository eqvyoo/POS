package com.dlnl.deliveryguard.repository;

import com.dlnl.deliveryguard.domain.SmsSendHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SmsSendHistoryRepository extends JpaRepository<SmsSendHistory, Long> {
    @Query("SELECT h FROM SmsSendHistory h WHERE h.user.id = :userId "
            + "AND (:customerContact IS NULL OR h.customerContact LIKE %:customerContact%) "
            + "AND (:sendTime IS NULL OR h.sendTime = :sendTime) "
            + "AND (:sendStatus IS NULL OR h.sendStatus = :sendStatus)")
    List<SmsSendHistory> findByUserIdAndFilters(
            @Param("userId") Long userId,
            @Param("customerContact") String customerContact,
            @Param("sendTime") LocalDateTime sendTime,
            @Param("sendStatus") String sendStatus);
}