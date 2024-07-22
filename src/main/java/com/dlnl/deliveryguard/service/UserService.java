package com.dlnl.deliveryguard.service;

import com.dlnl.deliveryguard.domain.Role;
import com.dlnl.deliveryguard.domain.User;
import com.dlnl.deliveryguard.domain.UserRole;
import com.dlnl.deliveryguard.jwt.JwtUtil;
import com.dlnl.deliveryguard.repository.RoleRepository;
import com.dlnl.deliveryguard.repository.UserRepository;
import com.dlnl.deliveryguard.repository.UserRoleRepository;
import com.dlnl.deliveryguard.web.LoginRequest;
import com.dlnl.deliveryguard.web.LoginResponse;
import com.dlnl.deliveryguard.web.UserRegistrationRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
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
}

    //todo: 토큰 로그인

    //todo: 사용자 정보 조회

    //todo: 구독정보 갱신

    //todo: 사용자 비밀번호 변경



