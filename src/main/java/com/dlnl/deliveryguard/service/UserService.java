package com.dlnl.deliveryguard.service;

import com.dlnl.deliveryguard.domain.Role;
import com.dlnl.deliveryguard.domain.User;
import com.dlnl.deliveryguard.domain.UserRole;
import com.dlnl.deliveryguard.jwt.JwtUtil;
import com.dlnl.deliveryguard.repository.RoleRepository;
import com.dlnl.deliveryguard.repository.UserRepository;
import com.dlnl.deliveryguard.repository.UserRoleRepository;
import com.dlnl.deliveryguard.web.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
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
    private final RoleService roleService;
    private final UserRoleService userRoleService;
    private final JwtUtil jwtUtil;

    public User findUserById(Long id){
        Optional<User> optionalUser = userRepository.findById(id);
        if (optionalUser.isPresent()) {
            return optionalUser.get();
        } else {
            throw new RuntimeException("User not found with id: " + id);
        }
    }

    public String registerNewUser(UserRegistrationRequest request) {

        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .isSubValid(true)
                .subExpiredAt(Date.from(LocalDate.now().plusDays(30).atStartOfDay(ZoneId.systemDefault()).toInstant()))
                .build();

        user = userRepository.save(user);
        String refreshToken = jwtUtil.generateRefreshToken(user.getId());
        user.updateRefreshToken(refreshToken);
        userRepository.save(user);

        Role role;
        try {
            role = roleService.findByName("USER");
        } catch (RuntimeException e) {
            role = roleService.createRole("USER");
        }
        userRoleService.saveUserRole(user, role);

        return "사용자 " + user.getUsername() + " 등록 완료";

    }

    @Transactional
    public String registerAdminUser(String username, String password) {
        User user = User.builder()
                .username(username)
                .password(passwordEncoder.encode(password))
                .isSubValid(true)
                .subExpiredAt(Date.from(LocalDate.now().plusDays(30).atStartOfDay(ZoneId.systemDefault()).toInstant()))
                .build();

        user = userRepository.save(user);
        String refreshToken = jwtUtil.generateRefreshToken(user.getId());
        user.updateRefreshToken(refreshToken);
        userRepository.save(user); // refreshToken 저장

        Role adminRole;
        try {
            adminRole = roleService.findByName("ADMIN");
        } catch (RuntimeException e) {
            adminRole = roleService.createRole("ADMIN");
        }

        userRoleService.saveUserRole(user, adminRole);

        return "관리자 " + user.getUsername() + " 등록 완료";
    }
    @Transactional
    public void updateRefreshToken (Long id, String refreshToken){
        User user = findUserById(id);
        if (user != null) {
            user.updateRefreshToken(refreshToken);
            userRepository.save(user);
        }
    }

    public User findByUsername (String username){
        Optional<User> optionalUser = userRepository.findByUsername(username);
        if (optionalUser.isPresent()) {
            return optionalUser.get();
        } else {
            throw new RuntimeException(username + " User not found");
        }
    }


    public LoginResponse authenticateUser (LoginRequest loginRequest) throws Exception {
        User user = findByUsername(loginRequest.getUsername());
        if (user == null || !passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            throw new BadCredentialsException("아이디 또는 비밀번호가 잘못 되었습니다. 아이디와 비밀번호를 정확히 입력해 주세요.");
        }
        Long id = user.getId();
        final String accessToken = jwtUtil.generateAccessToken(id);
        final String refreshToken = jwtUtil.generateRefreshToken(id);

        updateRefreshToken(id, refreshToken);

        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    @Transactional
    public LoginResponse loginWithRefreshToken(String refreshToken) {
        if (!jwtUtil.validateToken(refreshToken)) {
            throw new BadCredentialsException("Invalid Refresh Token");
        }

        Long userId = jwtUtil.getIdFromToken(refreshToken);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        String newAccessToken = jwtUtil.generateAccessToken(userId);
        String newRefreshToken = jwtUtil.generateRefreshToken(userId);
        user.updateUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        return LoginResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .build();
    }

    @Transactional
    public LoginResponse refreshAccessToken(String refreshToken) {
        if (!jwtUtil.validateToken(refreshToken)) {
            throw new BadCredentialsException("Invalid Refresh Token");
        }

        Long userId = jwtUtil.getIdFromToken(refreshToken);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        String newAccessToken = jwtUtil.generateAccessToken(userId);
        user.updateUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
        return LoginResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(refreshToken)
                .build();
    }

    @Transactional
    public void updateSubscriptions(List<SubscriptionUpdateRequest> requests) {
        for (SubscriptionUpdateRequest request : requests) {
            User user = findUserById(request.getUserId());
            user.updateUpdatedAt(LocalDateTime.now());
            if (request.getIsSubValid() != null && !request.getIsSubValid()) {
                user.updateSubExpiredAt(new Date());
                user.updateIsSubValid(false);
            } else {
                user.updateSubExpiredAt(request.getSubExpiredAt());
            }
            userRepository.save(user);
        }
    }

    public UserInfoResponse getUserInfo(String jwtToken) {
        if (!jwtUtil.validateToken(jwtToken)) {
            throw new RuntimeException("Invalid JWT token");
        }

        Long userId = jwtUtil.getIdFromToken(jwtToken);
        User user = findUserById(userId);
        String isValid;

        if (user.getIsSubValid()){
            isValid = "참";
        }else{
            isValid = "거짓";
        }

        return UserInfoResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .isvalid(isValid)
                .subExpiredAt(user.getSubExpiredAt())
                .build();

    }

}

    //todo: 사용자 비밀번호 변경



