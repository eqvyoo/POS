package com.dlnl.deliveryguard.web.Controller;

import com.dlnl.deliveryguard.service.UserService;
import com.dlnl.deliveryguard.web.DTO.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
public class UserController {
    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public ResponseEntity<String> registerUser(@RequestBody RegistrationRequest registrationRequest) {
        try {
            userService.registerUser(registrationRequest);
            return new ResponseEntity<>("회원가입이 성공적으로 완료되었습니다.", HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest loginRequest) {
        LoginResponse loginResponse = userService.login(loginRequest);
        return new ResponseEntity<>(loginResponse, HttpStatus.OK);
    }

    @PostMapping("/reissue")
    public ResponseEntity<TokenResponse> reissueToken(@RequestHeader("Authorization") String accessToken,
                                                      @RequestHeader("Refresh-Token") String refreshToken) {
        String actualAccessToken = accessToken.substring(7);
        TokenResponse tokenResponse = userService.reissueToken(actualAccessToken, refreshToken);
        return ResponseEntity.ok(tokenResponse);
    }

    @GetMapping("/validate-token")
    public ResponseEntity<String> validateAccessToken(@RequestHeader("Authorization") String accessToken) {
        String actualAccessToken = accessToken.substring(7);
        if (userService.validateAccessToken(actualAccessToken)) {
            return ResponseEntity.ok("토큰이 유효합니다.");
        } else {
            return ResponseEntity.status(401).body("토큰이 만료되었습니다.");
        }
    }


}

