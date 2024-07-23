package com.dlnl.deliveryguard.web.Controller;

import com.dlnl.deliveryguard.service.UserService;
import com.dlnl.deliveryguard.web.DTO.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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
        try {
            String responseMessage = userService.registerNewUser(request);
            return ResponseEntity.ok(responseMessage);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
    @PostMapping("/admin")
    public ResponseEntity<?>  createAdminUser(@RequestBody UserRegistrationRequest request) {
        try {
            String response = userService.registerAdminUser(request.getUsername(), request.getPassword());
            return ResponseEntity.ok(response);
        }catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        try {
            LoginResponse loginResponse = userService.authenticateUser(loginRequest);
            return ResponseEntity.ok(loginResponse);
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PostMapping("/reissue-token")
    public ResponseEntity<?> refreshAccessToken(@RequestHeader("Authorization") String token) {
        try {
            ReissueAccessTokenResponse response = userService.reissueAccessToken(token);
            return ResponseEntity.ok(response);
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
            UserInfoResponse userInfoResponse = userService.getUserInfo(token);
            return ResponseEntity.ok(userInfoResponse);
        } catch (Exception e) {
            return ResponseEntity.status(400).body(e.getMessage());
        }
    }

    @PutMapping("/update-password")
    public ResponseEntity<?> updatePassword(@RequestHeader("Authorization") String token,
                                            @RequestBody PasswordUpdateRequest request) {
        try {
            PasswordUpdateResponse passwordUpdateResponse= userService.updatePassword(token, request);
            return ResponseEntity.ok(passwordUpdateResponse);
        } catch (Exception e) {
            return ResponseEntity.status(400).body(e.getMessage());
        }
    }
}

