package com.dlnl.deliveryguard.web.Controller;

import com.dlnl.deliveryguard.service.UserService;
import com.dlnl.deliveryguard.web.DTO.*;
import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
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

    @PostMapping("/send-user-id")
    public ResponseEntity<String> sendUserIdToEmail(@RequestParam String email) {
        try {
            userService.sendUserIdToEmail(email);
            return ResponseEntity.ok("사용자 ID가 이메일로 전송되었습니다.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (MessagingException e) {
            return ResponseEntity.status(500).body("이메일 전송 중 오류가 발생했습니다.");
        }
    }
    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@RequestParam String email) {
        try {
            userService.resetPasswordAndSendEmail(email);
            return ResponseEntity.ok("새 비밀번호가 이메일로 전송되었습니다.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (MessagingException e) {
            return ResponseEntity.status(500).body("이메일 전송 중 오류가 발생했습니다.");
        }
    }

    @PostMapping("/update-password")
    public ResponseEntity<String> updatePassword(@AuthenticationPrincipal UserDetails userDetails, @RequestBody PasswordUpdateRequest request) {
        try {
            Long userId = userService.getUserIdFromUserDetails(userDetails);
            userService.updatePassword(userId, request.getNewPassword());
            return ResponseEntity.ok("비밀번호가 성공적으로 변경되었습니다.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();  // 예외 로그 출력
            return ResponseEntity.status(500).body("비밀번호 변경 중 오류가 발생했습니다.");
        }
    }


}

