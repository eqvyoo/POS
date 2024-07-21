package com.dlnl.deliveryguard.web;

import com.dlnl.deliveryguard.domain.User;
import com.dlnl.deliveryguard.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}

