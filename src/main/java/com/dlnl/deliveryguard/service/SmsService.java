package com.dlnl.deliveryguard.service;

import com.dlnl.deliveryguard.domain.SmsSendCondition;
import com.dlnl.deliveryguard.domain.User;
import com.dlnl.deliveryguard.repository.SmsSendConditionRepository;
import com.dlnl.deliveryguard.repository.SmsSendHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

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

    public SmsSendCondition updateSmsCondition(Long id, SmsSendCondition conditionDetails, User user) {
        Optional<SmsSendCondition> existingConditionOpt = conditionRepository.findById(id);

        if (existingConditionOpt.isPresent() && existingConditionOpt.get().getUser().getId().equals(user.getId())) {
            SmsSendCondition existingCondition = existingConditionOpt.get();

            existingCondition.updateSendCondition(conditionDetails.getSendCondition());
            existingCondition.updateMessageContent(conditionDetails.getMessageContent());
            existingCondition.updateUser(user);

            return conditionRepository.save(existingCondition);
        } else {
            throw new RuntimeException("조건을 찾을 수 없습니다.");
        }
    }
}
