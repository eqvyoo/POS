package com.dlnl.deliveryguard.web.Controller;

import com.dlnl.deliveryguard.domain.SmsSendCondition;
import com.dlnl.deliveryguard.service.SmsService;
import com.dlnl.deliveryguard.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

}
