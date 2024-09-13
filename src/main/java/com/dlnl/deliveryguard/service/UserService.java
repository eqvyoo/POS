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





//    public String registerNewUser(UserRegistrationRequest request) throws Exception{
//        if(userRepository.findByUsername(request.getUsername()).isPresent()){
//            throw new Exception("해당 아이디는 사용하실 수 없습니다.");
//        }
//
//        User user = User.builder()
//                .username(request.getUsername())
//                .password(passwordEncoder.encode(request.getPassword())).build();
//
//        user = userRepository.save(user);
//        String refreshToken = jwtUtil.generateRefreshToken(user.getId());
//        user.updateRefreshToken(refreshToken);
//        userRepository.save(user);
//
//
//        return "사용자 " + user.getUsername() + " 등록 완료";
//
//    }
//
//    @Transactional
//    public String registerAdminUser(String username, String password) throws Exception{
//        if(userRepository.findByUsername(username).isPresent()){
//            throw new Exception("해당 아이디는 사용하실 수 없습니다.");
//        }
//        User user = User.builder()
//                .username(username)
//                .password(passwordEncoder.encode(password))
//                .build();
//
//        user = userRepository.save(user);
//        String refreshToken = jwtUtil.generateRefreshToken(user.getId());
//        user.updateRefreshToken(refreshToken);
//        userRepository.save(user);
//
//
//        return "관리자 " + user.getUsername() + " 등록 완료";
//    }
//
//    private User findByUsername(String username) {
//        Optional<User> optionalUser = userRepository.findByUsername(username);
//        if (optionalUser.isPresent()) {
//            return optionalUser.get();
//        } else {
//            throw new RuntimeException(username + " 해당되는 아이디가 없습니다.");
//        }
//    }
//
//
//    public LoginResponse authenticateUser(LoginRequest loginRequest) throws Exception {
//        User user = findByUsername(loginRequest.getUsername());
//        if (user == null || !passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
//            throw new BadCredentialsException("아이디 또는 비밀번호가 잘못 되었습니다. 아이디와 비밀번호를 정확히 입력해 주세요.");
//        }
//        Long id = user.getId();
//        final String accessToken = jwtUtil.generateAccessToken(id);
//
//        return LoginResponse.builder()
//                .accessToken(accessToken)
//                .refreshToken(user.getRefreshToken())
//                .build();
//    }
//
//
//    @Transactional
//    public ReissueAccessTokenResponse reissueAccessToken(String token) {
//        String refreshToken = token.substring(7);
//        jwtUtil.validateRefreshToken(refreshToken);
//        Long userId = jwtUtil.getIdFromToken(refreshToken);
//        User user = findUserById(userId);
//        String newAccessToken = jwtUtil.generateAccessToken(userId);
//        user.updateUpdatedAt(LocalDateTime.now());
//        userRepository.save(user);
//        return ReissueAccessTokenResponse.builder()
//                .accessToken(newAccessToken)
//                .build();
//    }
//
//    @Transactional
//    public void updateSubscriptions(List<SubscriptionUpdateRequest> requests) {
//        for (SubscriptionUpdateRequest request : requests) {
//            User user = findByUsername(request.getUsername());
//            user.updateUpdatedAt(LocalDateTime.now());
//
//            userRepository.save(user);
//        }
//    }
//
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



