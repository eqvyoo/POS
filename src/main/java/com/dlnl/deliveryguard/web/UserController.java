package com.dlnl.deliveryguard.web;

import com.dlnl.deliveryguard.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class UserController {
    @Autowired
    private UserService userService;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/register")
    public ResponseEntity<String> registerUser(@RequestBody UserRegistrationRequest request) {
        String responseMessage = userService.registerNewUser(request);
        return ResponseEntity.ok(responseMessage);
    }
    @PostMapping("/admin")
    public String createAdminUser(@RequestBody UserRegistrationRequest request) {
        return userService.registerAdminUser(request.getUsername(), request.getPassword());
    }
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        try {
            LoginResponse loginResponse = userService.authenticateUser(loginRequest);
            return ResponseEntity.ok(loginResponse);
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(401).body("Invalid username or password");
        } catch (Exception e) {
            throw new RuntimeException("유효하지 않은 접근입니다", e);
        }
    }

    @PostMapping("/token-login")
    public ResponseEntity<?> loginWithRefreshToken(@RequestBody TokenRequest tokenRequest) {
        try {
            LoginResponse loginResponse = userService.loginWithRefreshToken(tokenRequest.getRefreshToken());
            return ResponseEntity.ok(loginResponse);
        } catch (Exception e) {
            return ResponseEntity.status(401).body(e.getMessage());
        }
    }
    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshAccessToken(@RequestBody TokenRequest tokenRequest) {
        try {
            LoginResponse loginResponse = userService.refreshAccessToken(tokenRequest.getRefreshToken());
            return ResponseEntity.ok(loginResponse);
        } catch (Exception e) {
            return ResponseEntity.status(401).body(e.getMessage());
        }
    }

    @PostMapping("/update-subscriptions")
    public ResponseEntity<?> updateSubscriptions(@RequestBody List<SubscriptionUpdateRequest> requests) {
        try {
            userService.updateSubscriptions(requests);
            return ResponseEntity.ok("Subscriptions updated successfully");
        } catch (Exception e) {
            return ResponseEntity.status(400).body(e.getMessage());
        }
    }
    @GetMapping("/info")
    public ResponseEntity<?> getUserInfo(@RequestHeader("Authorization") String token) {
        try {
            String jwtToken = token.substring(7); // "Bearer " 부분 제거
            UserInfoResponse userInfoResponse = userService.getUserInfo(jwtToken);
            return ResponseEntity.ok(userInfoResponse);
        } catch (Exception e) {
            return ResponseEntity.status(400).body(e.getMessage());
        }
    }
}

