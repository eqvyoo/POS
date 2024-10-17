package com.dlnl.deliveryguard.web.Controller;

import com.dlnl.deliveryguard.domain.SmsSendCondition;
import com.dlnl.deliveryguard.domain.User;
import com.dlnl.deliveryguard.service.SmsService;
import com.dlnl.deliveryguard.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

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



}



