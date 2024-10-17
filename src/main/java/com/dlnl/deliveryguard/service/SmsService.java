package com.dlnl.deliveryguard.service;

import com.dlnl.deliveryguard.domain.SmsSendCondition;
import com.dlnl.deliveryguard.domain.SmsSendHistory;
import com.dlnl.deliveryguard.domain.User;
import com.dlnl.deliveryguard.repository.SmsSendConditionRepository;
import com.dlnl.deliveryguard.repository.SmsSendHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SmsService {
    private final SmsSendConditionRepository conditionRepository;
    private final SmsSendHistoryRepository historyRepository;

    public List<SmsSendCondition> getSendConditionsByUserId(Long userId) {
        return conditionRepository.findByUserIdAndIsDeletedFalse(userId);
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

    public void deleteSmsCondition(Long id, User user) {
        Optional<SmsSendCondition> existingCondition = conditionRepository.findById(id);

        if (existingCondition.isPresent() && existingCondition.get().getUser().getId().equals(user.getId())) {
            SmsSendCondition condition = existingCondition.get();
            condition.markAsDeleted();
            conditionRepository.save(condition);
        } else {
            throw new RuntimeException("문자 전송 조건이 정상적으로 삭제되지 않았습니다.");
        }
    }

    public SmsSendHistory createSmsSendHistory(SmsSendHistory sendHistoryDetails, User user) {
        try {
            sendHistoryDetails.updateUser(user);
            SmsSendHistory savedHistory = historyRepository.save(sendHistoryDetails);

            return savedHistory;
        } catch (Exception e) {
            throw new RuntimeException("문자 전송 내역 저장 중 문제가 발생했습니다. 다시 시도해주세요.");
        }
    }

    public List<SmsSendHistory> searchSmsSendHistory(Long userId, String customerContact, LocalDateTime sendTime, String sendStatus) {
        try {
            return historyRepository.findByUserIdAndFilters(userId, customerContact, sendTime, sendStatus);
        } catch (Exception e) {
            throw new RuntimeException("문자 전송 내역 불러오기에 실패했습니다. 다시 시도해주세요.");
        }
    }

}
