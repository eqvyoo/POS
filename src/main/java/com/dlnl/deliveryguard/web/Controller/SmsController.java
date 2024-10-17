package com.dlnl.deliveryguard.web.Controller;

import com.dlnl.deliveryguard.domain.SmsSendCondition;
import com.dlnl.deliveryguard.domain.SmsSendHistory;
import com.dlnl.deliveryguard.domain.User;
import com.dlnl.deliveryguard.service.SmsService;
import com.dlnl.deliveryguard.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/sms")
public class SmsController {

    private final SmsService smsService;

    private final UserService userService;

    @GetMapping("/all-conditions")
    public ResponseEntity<List<SmsSendCondition>> getAllSendConditions(
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = userService.getUserIdFromUserDetails(userDetails);
        return ResponseEntity.ok(smsService.getSendConditionsByUserId(userId));
    }
    @PostMapping("/create-condition")
    public ResponseEntity<SmsSendCondition> createOrUpdateCondition(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody SmsSendCondition condition) {
        Long userId = userService.getUserIdFromUserDetails(userDetails);
        User user = userService.findUserById(userId);
        return ResponseEntity.ok(smsService.createSmsCondition(condition, user));
    }

    @PutMapping("/update-condition/{id}")
    public ResponseEntity<SmsSendCondition> updateCondition(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id,
            @RequestBody SmsSendCondition conditionDetails) {
        Long userId = userService.getUserIdFromUserDetails(userDetails);
        User user = userService.findUserById(userId);
        return ResponseEntity.ok(smsService.updateSmsCondition(id, conditionDetails, user));
    }

    @DeleteMapping("/delete-condition/{id}")
    public ResponseEntity<String> deleteCondition(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        Long userId = userService.getUserIdFromUserDetails(userDetails);
        User user = userService.findUserById(userId);
        smsService.deleteSmsCondition(id, user);
        return ResponseEntity.ok("문자 전송 조건 삭제가 완료되었습니다.");
    }

    @PostMapping("/create-send-history")
    public ResponseEntity<SmsSendHistory> createSendHistory(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody SmsSendHistory sendHistoryDetails) {
        Long userId = userService.getUserIdFromUserDetails(userDetails);
        User user = userService.findUserById(userId);
        return ResponseEntity.ok(smsService.createSmsSendHistory(sendHistoryDetails, user));
    }

    @GetMapping("/search-send-history")
    public ResponseEntity<List<SmsSendHistory>> searchSendHistory(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) String customerContact,
            @RequestParam(required = false) LocalDateTime sendTime,
            @RequestParam(required = false) String sendStatus) {
        Long userId = userService.getUserIdFromUserDetails(userDetails);
        List<SmsSendHistory> result = smsService.searchSmsSendHistory(userId, customerContact, sendTime, sendStatus);
        return ResponseEntity.ok(result);
    }



}



