package com.dlnl.deliveryguard.service;

import com.dlnl.deliveryguard.domain.Role;
import com.dlnl.deliveryguard.domain.User;
import com.dlnl.deliveryguard.jwt.JwtUtil;
import com.dlnl.deliveryguard.repository.UserRepository;
import com.dlnl.deliveryguard.web.DTO.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public User findUserById(Long id) {
        Optional<User> optionalUser = userRepository.findById(id);
        if (optionalUser.isPresent()) {
            return optionalUser.get();
        } else {
            throw new RuntimeException(id + "번 사용자는 존재하지 않습니다.");
        }
    }

    @Transactional
    public void registerUser(RegistrationRequest registrationRequest) {
        if (userRepository.existsByLoginID(registrationRequest.getLoginID())) {
            throw new IllegalArgumentException("이미 존재하는 id입니다.");
        }

        if (userRepository.existsByEmail(registrationRequest.getEmail())) {
            throw new IllegalArgumentException("이미 존재하는 이메일입니다.");
        }

        if (!isValidPassword(registrationRequest.getPassword())) {
            throw new IllegalArgumentException("비밀번호는 영문과 숫자를 포함한 8자리 이상이어야 합니다.");
        }

        String encodedPassword = passwordEncoder.encode(registrationRequest.getPassword());

        User user = User.builder()
                .loginID(registrationRequest.getLoginID())
                .password(encodedPassword)
                .userName(registrationRequest.getUsername())
                .phoneNumber(registrationRequest.getPhoneNumber())
                .email(registrationRequest.getEmail())
                .storeName(registrationRequest.getStoreName())
                .storeAddress(registrationRequest.getStoreAddress())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .role(Role.USER)
                .build();

        userRepository.save(user);
    }

    private boolean isValidPassword(String password) {
        return password.length() >= 8 && password.matches(".*[A-Za-z].*") && password.matches(".*[0-9].*");
    }

    @Transactional
    public LoginResponse login(LoginRequest loginRequest) {
        User user = userRepository.findByLoginID(loginRequest.getLoginID())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        String accessToken = jwtUtil.generateAccessToken(user.getId());
        String refreshToken = jwtUtil.generateRefreshToken(user.getId());

        user.updateRefreshToken(refreshToken);
        user.updateUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    @Transactional
    public TokenResponse reissueToken(String expiredAccessToken, String refreshToken) {

        if (!jwtUtil.validateRefreshToken(refreshToken)) {
            throw new IllegalArgumentException("유효하지 않은 리프레시 토큰입니다.");
        }

        Long userId = jwtUtil.getIdFromToken(refreshToken);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // DB에 저장된 Refresh token과 일치하는지 확인
        if (!user.getRefreshToken().equals(refreshToken)) {
            throw new IllegalArgumentException("리프레시 토큰이 일치하지 않습니다.");
        }

        // Access token 재발급
        String newAccessToken = jwtUtil.generateAccessToken(user.getId());
        String newRefreshToken = jwtUtil.generateRefreshToken(user.getId());

        // 새로운 Refresh token을 업데이트 후 저장
        user.updateRefreshToken(newRefreshToken);
        user.updateUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        return TokenResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .build();
    }


    public boolean validateAccessToken(String token) {
        return jwtUtil.validateToken(token);
    }

//    public UserInfoResponse getUserInfo(String token) {
//        String jwtToken = token.substring(7);
//        jwtUtil.validateToken(jwtToken);
//        Long userId = jwtUtil.getIdFromToken(jwtToken);
//        User user = findUserById(userId);
//
//
//        return UserInfoResponse.builder()
//                .id(user.getId())
//                .username(user.getUsername())
//                .build();
//
//    }
//    @Transactional
//    public PasswordUpdateResponse updatePassword(String token, PasswordUpdateRequest request) {
//        String jwtToken = token.substring(7);
//        jwtUtil.validateToken(jwtToken);
//
//        User user = findByUsername(request.getUsername());
//        user.updatePassword(passwordEncoder.encode(request.getNewPassword()));
//        user.updateUpdatedAt(LocalDateTime.now());
//        userRepository.save(user);
//
//        return PasswordUpdateResponse.builder()
//                .id(user.getId())
//                .username(user.getUsername())
//                .build();
//    }

}



