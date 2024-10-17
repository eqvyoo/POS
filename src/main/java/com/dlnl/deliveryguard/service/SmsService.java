package com.dlnl.deliveryguard.service;

import com.dlnl.deliveryguard.domain.SmsSendCondition;
import com.dlnl.deliveryguard.domain.User;
import com.dlnl.deliveryguard.repository.SmsSendConditionRepository;
import com.dlnl.deliveryguard.repository.SmsSendHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SmsService {
    private final SmsSendConditionRepository conditionRepository;
    private final SmsSendHistoryRepository historyRepository;

    public List<SmsSendCondition> getSendConditionsByUserId(Long userId) {
        return conditionRepository.findByUserId(userId);
    }

    public SmsSendCondition createSmsCondition(SmsSendCondition condition, User user) {
        condition.updateUser(user);
        return conditionRepository.save(condition);
    }
}
